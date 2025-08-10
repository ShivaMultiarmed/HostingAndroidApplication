package mikhail.shell.video.hosting.data.repositories

import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.google.common.net.HttpHeaders
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.ValidationException
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.errors.video.VideoEditingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import retrofit2.HttpException
import java.io.File
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

const val TRANSFER_BUFFER_SIZE = 10 * 1024 * 1024

class VideoRepositoryWithApi @Inject constructor(
    private val videoApi: VideoApi,
    private val gson: Gson,
    private val fileProvider: FileProvider
) : VideoRepository {
    override suspend fun fetchVideoInfo(videoId: Long): Result<Video, Error> = request {
        videoApi.fetchVideo(videoId).toDomain()
    }

    override suspend fun fetchVideoRecommendations(
        partIndex: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error> {
        return request {
            videoApi
                .fetchVideoRecommendationsPart(partIndex, partSize)
                .map { it.toDomain() }
        }
    }

    override suspend fun fetchVideoDetails(
        videoId: Long,
        userId: Long
    ): Result<VideoDetails, Error> = request {
        videoApi.fetchVideoDetails(videoId, userId).toDomain()
    }

    override suspend fun rateVideo(
        videoId: Long,
        liking: LikingState
    ): Result<Unit, Error> = request {
        videoApi.rateVideo(videoId, liking)
    }

    override suspend fun fetchChannelVideoList(
        channelId: Long,
        partNumber: Long,
        partSize: Int
    ): Result<List<Video>, Error> = request {
        videoApi.fetchVideoList(
            channelId = channelId,
            partNumber = partNumber,
            partSize = partSize
        ).map { it.toDomain() }
    }

    override suspend fun fetchVideosWithChannelsByQuery(
        query: String,
        partNumber: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error> = request {
        videoApi.fetchVideoListByQuery(
            query = query,
            partNumber = partNumber,
            partSize = partSize
        ).map { it.toDomain() }
    }

    override suspend fun uploadVideo(
        video: Video,
        source: String,
        cover: String?,
        onVideoCreated: (Video) -> Unit,
        onProgress: (Float) -> Unit
    ): Result<Video, Error> {
        return try {
            val sourceUri = source.toUri()
            val sourceMime = fileProvider.getFileMimeType(sourceUri)
            val sourceExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(sourceMime)
            val sourceSize = fileProvider.getFileSize(sourceUri)!!
            val videoResponse = videoApi.uploadVideoDetails(video.toDto()).toDomain()
            onVideoCreated(videoResponse)
            var bytesTransferred = 0
            val sourceInputStream = fileProvider.getFileAsInputStream(sourceUri)
            sourceInputStream!!.process { bytesRead, buffer ->
                videoApi.uploadVideoSource(
                    videoId = videoResponse.videoId!!,
                    extension = sourceExtension!!,
                    source = buffer.toOctetStream(bytesRead)
                )
                bytesTransferred += bytesRead
                val progress = bytesTransferred.toFloat() / sourceSize
                onProgress(progress)
            }
            cover?.let { notNullCover ->
                val coverUri = notNullCover.toUri()
                val coverMime = fileProvider.getFileMimeType(coverUri)!!
                val coverExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(coverMime)!!
                val coverContent = fileProvider.getFileAsInputStream(coverUri)?.use {
                    it.readBytes().toOctetStream()
                }
                videoApi.uploadVideoCover(
                    videoResponse.videoId!!,
                    coverExtension,
                    coverContent!!
                )
            }
            videoApi.confirmVideoUpload(videoResponse.videoId!!)
            Result.Success(videoResponse)
        } catch (e: HttpException) {
            val error = when (e.code()) {
                401 -> NetworkError.AUTHENTICATION
                404 -> NetworkError.NOT_FOUND
                500 -> NetworkError.SERVER_ERROR
                else -> UnexpectedError
            }
            Result.Failure(error)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val error = when (e) {
                is SocketTimeoutException -> NetworkError.TIMEOUT_EXCEEDED
                is ConnectException -> NetworkError.CONNECTION_ERROR
                is IOException -> NetworkError.SERVER_NOT_AVAILABLE
                else -> UnexpectedError
            }
            Result.Failure(error)
        }
    }

    override suspend fun incrementViews(videoId: Long): Result<Unit, Error> = request {
        videoApi.incrementViews(videoId)
    }

    override suspend fun deleteVideo(videoId: Long): Result<Unit, Error> = request {
        videoApi.deleteVideo(videoId)
    }

    override suspend fun editVideo(
        video: Video,
        coverAction: EditAction,
        cover: File?
    ): Result<Video, Error> = request (
        httpExceptionHandler(400) { e ->
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<VideoEditingError>>() {}.type
            gson.fromJson<CompoundError<VideoEditingError>>(json, type)?: UnexpectedError
        }
    ) {
        val compoundError = CompoundError<VideoEditingError>()
        cover?.let {
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
            if (!it.exists()) {
                compoundError.add(VideoEditingError.COVER_NOT_FOUND)
            } else if (!mime!!.contains("image")) {
                compoundError.add(VideoEditingError.COVER_TYPE_NOT_VALID)
            } else if (it.length() > ValidationRules.MAX_IMAGE_SIZE) {
                compoundError.add(VideoEditingError.COVER_TOO_LARGE)
            }
        }
        if (compoundError.isNotNull()) {
            throw ValidationException(compoundError)
        }
        val coverPart = cover?.toPart("cover")
        videoApi.editVideo(video.toDto(), coverAction, coverPart).toDomain()
    }

    override suspend fun downloadVideo(
        videoId: Long,
        onPartitionLoaded: (String, Long, Array<Byte>) -> Unit
    ): Result<Unit, Error> {
        try {
            val range = 1024 * 1024 * 10
            var start = 0
            var end = range - 1
            var size: Long? = null
            var mime: String? = null
            do {
                var response = videoApi.downloadVideo(
                    videoId = videoId,
                    byteRange = "bytes=$start-$end"
                )
                if (!response.isSuccessful) {
                    if (response.code() == 401) {
                        return Result.Failure(NetworkError.AUTHENTICATION)
                    } else if (response.code() == 404) {
                        return Result.Failure(NetworkError.NOT_FOUND)
                    } else if (response.code() == 416) { // 416 status code: range not satisfiable - end of file is passed
                        response = videoApi.downloadVideo(
                            videoId = videoId,
                            byteRange = "bytes=$start-"
                        )
                        if (response.body() == null) {
                            return Result.Failure(UnexpectedError)
                        }
                    } else if (response.code() == 500) {
                        return Result.Failure(NetworkError.SERVER_ERROR)
                    } else {
                        return Result.Failure(UnexpectedError)
                    }
                }
                if (size == null) {
                    size = response.headers()[HttpHeaders.CONTENT_RANGE]!!.parseFileSize()
                }
                if (mime == null) {
                    mime = response.headers()[HttpHeaders.CONTENT_TYPE]
                }
                val bytes = response.body()!!.bytes()
                onPartitionLoaded(mime!!, size, bytes.toTypedArray())
                start = end + 1
                end = start + range - 1
            } while (start < size)
            return Result.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val error = when (e) {
                is SocketTimeoutException -> NetworkError.TIMEOUT_EXCEEDED
                is ConnectException -> NetworkError.CONNECTION_ERROR
                is IOException -> NetworkError.SERVER_NOT_AVAILABLE
                else -> UnexpectedError
            }
            return Result.Failure(error)
        }
    }

}

fun FileProvider.uriToPart(uriStr: String, partName: String): MultipartBody.Part {
    val uri = uriStr.toUri()
    val mimeType = getFileMimeType(uri)
    val extension = MimeTypeMap
        .getSingleton()
        .getExtensionFromMimeType(mimeType)
    val bytes = getFileAsInputStream(uri)!!.use {
        it.readBytes()
    }
    val fileName = "$partName.$extension"
    val requestBody = RequestBody.create(
        mimeType?.toMediaTypeOrNull(),
        bytes
    )
    return MultipartBody.Part.createFormData(partName, fileName, requestBody)
}

fun File.toPart(partName: String): MultipartBody.Part {
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension)!!
    val requestBody = StreamedRequestBody(this, mimeType)
    return MultipartBody.Part.createFormData(partName, this.name, requestBody)
}

fun ByteArray.toOctetStream(nonNullBytesNumber: Int = this.size): RequestBody {
    return RequestBody.create(
        "application/octet-stream".toMediaTypeOrNull(),
        this,
        0,
        nonNullBytesNumber
    )
}

class StreamedRequestBody(
    val file: File,
    val mimeType: String = "application/octet-stream"
) : RequestBody() {

    override fun contentType() = mimeType.toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(TRANSFER_BUFFER_SIZE)
        var bytesRead: Int
        file.inputStream().buffered(TRANSFER_BUFFER_SIZE).use { input ->
            while (input.read(buffer).also { bytesRead = it } != -1) {
                sink.outputStream().write(buffer)
                sink.flush()
            }
        }
    }
}

suspend fun InputStream.process(
    onChunkRead: suspend (bytesRead: Int, buffer: ByteArray) -> Unit
) {
    this.use {
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
    return this.substringAfter("/").toLong()
}