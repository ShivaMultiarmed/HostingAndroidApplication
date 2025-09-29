package mikhail.shell.video.hosting.data.repositories

import android.webkit.MimeTypeMap
import com.google.common.net.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.data.dto.VideoEditingErrorResponse
import mikhail.shell.video.hosting.data.dto.VideoUploadingErrorResponse
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.parseFileSize
import mikhail.shell.video.hosting.data.utils.process
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.data.utils.toRequestBody
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.errors.video.VideoEditingError
import mikhail.shell.video.hosting.domain.errors.video.VideoUploadingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoForUser
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.models.VideoWithChannelForUser
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import okhttp3.MultipartBody
import okio.IOException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class VideoRepositoryWithApi @Inject constructor(
    private val videoApi: VideoApi,
    private val fileProvider: FileProvider
) : VideoRepository {

    override suspend fun fetchVideoInfo(videoId: Long): Result<Video, Error> = request {
        videoApi.fetchVideo(videoId).toDomain()
    }

    override suspend fun fetchVideoRecommendations(
        partIndex: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error> = request {
        videoApi.fetchVideoRecommendationsPart(
            partIndex = partIndex,
            partSize = partSize
        ).map { it.toDomain() }
    }

    override suspend fun fetchVideoDetails(videoId: Long): Result<VideoWithChannelForUser, Error> = request {
        videoApi.fetchVideoDetails(videoId).toDomain()
    }

    override suspend fun rateVideo(
        videoId: Long,
        liking: Liking
    ): Result<VideoForUser, Error> = request {
        videoApi.rateVideo(videoId, liking).toDomain()
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
        cursor: Long?,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error> = request {
        videoApi.fetchVideoListByQuery(
            query = query,
            partNumber = cursor,
            partSize = partSize
        ).map { it.toDomain() }
    }

    override suspend fun uploadVideo(
        video: Video,
        videoMetaData: VideoMetaData,
        cover: String?
    ): Result<Video, Error> {
        return request(
            httpExceptionHandler(400) {
                val response = Json.decodeFromString<VideoUploadingErrorResponse>(
                    it.response()?.body() as String
                )
                VideoUploadingError(
                    titleError = response.titleError,
                    sourceError = response.sourceError,
                    coverError = response.coverError,
                    descriptionError = response.descriptionError
                )
            }
        ) {
            val coverPart = cover?.let {
                val mime = fileProvider.getFileMimeType(it)!!
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                fileProvider.getFileAsInputStream(it)?.use {
                    it.readBytes().toRequestBody().let {
                        MultipartBody.Part.createFormData(
                            name = "cover",
                            filename = "cover.$extension",
                            body = it
                        )
                    }
                }
            }
            videoApi.uploadVideoDetails(
                video = VideoUploadingRequest(
                    title = video.title,
                    channelId = video.channelId,
                    description = video.description
                ),
                source = videoMetaData,
                cover = coverPart
            ).toDomain()
        }
    }

    override suspend fun uploadVideo(
        videoId: Long,
        source: String,
        onProgress: (Float) -> Unit
    ): Result<Unit, Error> {
        return try {
            val sourceSize = fileProvider.getFileSize(source)!!
            var bytesTransferred = 0
            var chunkIndex = 0L
            val sourceInputStream = fileProvider.getFileAsInputStream(source)
            val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            sourceInputStream!!.process { bytesRead, buffer ->
                coroutineScope.launch {
                    videoApi.uploadVideoSource(
                        videoId = videoId,
                        chunkIndex = chunkIndex,
                        source = buffer.toRequestBody(bytesNumber = bytesRead)
                    )
                    bytesTransferred += bytesRead
                    val progress = bytesTransferred.toFloat() / sourceSize
                    onProgress(progress)
                }
                chunkIndex++
            }
            videoApi.confirmVideoUpload(videoId)
            Result.Success(Unit)
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

    override suspend fun incrementViews(videoId: Long): Result<Video, Error> = request {
        videoApi.incrementViews(videoId).toDomain()
    }

    override suspend fun deleteVideo(videoId: Long): Result<Unit, Error> = request {
        videoApi.deleteVideo(videoId)
    }

    override suspend fun editVideo(
        video: Video,
        coverAction: EditAction,
        cover: String?
    ): Result<Video, Error> = request(
        httpExceptionHandler(400) {
            val response =
                Json.decodeFromString<VideoEditingErrorResponse>(it.response()?.body() as String)
            VideoEditingError(
                titleError = response.titleError,
                coverError = response.coverError,
                descriptionError = response.descriptionError,
                channelId = response.channelIdError
            )
        }
    ) {
        val coverPart = cover?.let {
            val mime = fileProvider.getFileMimeType(it)!!
            fileProvider.getFileAsInputStream(it)
                ?.use { it.readBytes() }!!
                .toRequestBody(mimeType = mime)
                .let {
                    val extension = MimeTypeMap
                        .getSingleton()
                        .getExtensionFromMimeType(mime)
                    MultipartBody.Part.createFormData(
                        name = "cover",
                        filename = "cover.$extension",
                        body = it
                    )
                }
        }
        videoApi.editVideo(
            video = VideoEditingRequest(
                videoId = video.videoId!!,
                title = video.title,
                channelId = video.channelId,
                description = video.description
            ),
            cover = coverPart
        ).toDomain()
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
                    } else if (response.code() == 416) { // 416 status code: range not satisfiable - the end of the file is passed
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

data class VideoUploadingRequest(
    val title: String,
    val channelId: Long,
    val description: String?
)

data class VideoMetaData(
    val fileName: String,
    val mimeType: String,
    val size: Long
)

data class VideoEditingRequest(
    val videoId: Long,
    val title: String,
    val channelId: Long,
    val description: String?
)