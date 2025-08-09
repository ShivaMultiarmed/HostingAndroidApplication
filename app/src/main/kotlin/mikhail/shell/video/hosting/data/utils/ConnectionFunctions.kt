package mikhail.shell.video.hosting.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.ValidationException
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.Result
import okio.IOException
import retrofit2.HttpException
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