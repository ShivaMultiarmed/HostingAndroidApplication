package mikhail.shell.video.hosting.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.ValidationException
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import java.io.File
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketTimeoutException

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

suspend fun <D> request(
    httpExceptionHandlers: Map<Int, (HttpException) -> Error> = emptyMap(),
    unexpectedExceptionHandler: (Exception) -> Error = unexpectedExceptionHandler { UnexpectedError },
    resultHandler: suspend () -> D
): Result<D, Error> {
    return try {
        Result.Success(resultHandler())
    } catch (e: HttpException) {
        val error = httpExceptionHandlers[e.code()]?.invoke(e)?: when (e.code()) {
            400 -> NetworkError.BAD_REQUEST
            401 -> NetworkError.AUTHENTICATION
            403 -> NetworkError.FORBIDDEN
            404 -> NetworkError.NOT_FOUND
            500 -> NetworkError.SERVER_ERROR
            else -> unexpectedExceptionHandler(e)
        }
        Result.Failure(error)
    } catch (e: ValidationException) {
        Result.Failure(e.error)
    } catch (e: Exception) {
        val error = when (e) {
            is SocketTimeoutException -> NetworkError.TIMEOUT_EXCEEDED
            is ConnectException -> NetworkError.CONNECTION_ERROR
            is IOException -> NetworkError.SERVER_NOT_AVAILABLE
            else -> unexpectedExceptionHandler(e)
        }
        Result.Failure(error)
    }
}

suspend fun <D> request(
    vararg httpExceptionHandlers: Pair<Int, (HttpException) -> Error>,
    unexpectedExceptionHandler: (Exception) -> Error = unexpectedExceptionHandler{ UnexpectedError },
    resultHandler: suspend () -> D
): Result<D, Error> {
    return request(
        httpExceptionHandlers.toMap(),
        unexpectedExceptionHandler,
        resultHandler
    )
}

fun httpExceptionHandler(
    code: Int,
    processing: (HttpException) -> Error
): Pair<Int, (HttpException) -> Error> {
    return code to processing
}

fun unexpectedExceptionHandler(
    processing: (Exception) -> Error
) = processing

const val TRANSFER_BUFFER_SIZE = 10 * 1024 * 1024

fun FileProvider.uriToPart(uriString: String, partName: String): MultipartBody.Part {
    val uri = uriString.toUri()
    val mimeType = getFileMimeType(uri)
    val extension = MimeTypeMap
        .getSingleton()
        .getExtensionFromMimeType(mimeType)
    val bytes = getFileAsInputStream(uri)!!.use { it.readBytes() }
    val fileName = "$partName.$extension"
    val requestBody = bytes.toRequestBody(
        contentType = mimeType?.toMediaTypeOrNull()
    )
    return MultipartBody.Part.createFormData(
        name = partName,
        filename = fileName,
        body = requestBody
    )
}

fun File.toPart(partName: String): MultipartBody.Part {
    val mimeType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension.lowercase())
        ?: "application/octet-stream"
    val requestBody = asRequestBody(
        contentType = mimeType.toMediaTypeOrNull()
    )
    return MultipartBody.Part.createFormData(
        name = partName,
        filename = name,
        body = requestBody
    )
}

fun ByteArray.toOctetStream(bytesNumber: Int = this.size): RequestBody {
    return toRequestBody(
        contentType = "application/octet-stream".toMediaTypeOrNull(),
        byteCount = bytesNumber
    )
}

suspend fun InputStream.process(
    onChunkRead: suspend (bytesRead: Int, buffer: ByteArray) -> Unit
) {
    use {
        val buffer = ByteArray(TRANSFER_BUFFER_SIZE)
        var curChunkNumber = 0
        var bytesRead: Int
        while (it.read(buffer).also { bytesRead = it } != -1) {
            onChunkRead(bytesRead, buffer)
            curChunkNumber++
        }
    }
}

fun String.parseFileSize(): Long { // from HTTP-header
    return substringAfter("/").toLong()
}