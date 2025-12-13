package mikhail.shell.video.hosting.data.repositories

import android.webkit.MimeTypeMap
import com.google.common.net.HttpHeaders
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.BuildConfig.API_BASE_URL
import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.data.dto.VideoEditingErrorResponse
import mikhail.shell.video.hosting.data.dto.VideoUploadingErrorResponse
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.parseFileSize
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.data.utils.toRequestBody
import mikhail.shell.video.hosting.data.utils.uriToPart
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.errors.video.VideoEditingError
import mikhail.shell.video.hosting.domain.errors.video.VideoUploadingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.EditingAction
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.PendingVideo
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoCreationModel
import mikhail.shell.video.hosting.domain.models.VideoEditingModel
import mikhail.shell.video.hosting.domain.models.VideoForUser
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.models.VideoWithChannelForUser
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import okio.IOException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.plusAssign
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class VideoRepositoryWithApi @Inject constructor(
    private val videoApi: VideoApi,
    private val fileProvider: FileProvider,
    private val gson: Gson
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

    override suspend fun fetchVideoDetails(videoId: Long): Result<VideoWithChannelForUser, Error> =
        request {
            videoApi.fetchVideoDetails(videoId).toDomain()
        }

    override suspend fun rateVideo(
        videoId: Long,
        liking: Liking
    ): Result<VideoForUser, Error> = request {
        videoApi.rateVideo(videoId, liking.name.lowercase()).toDomain()
    }

    override suspend fun fetchChannelVideoList(
        channelId: Long,
        partIndex: Long,
        partSize: Int
    ): Result<List<Video>, Error> = request {
        videoApi.fetchVideoList(
            channelId = channelId,
            partIndex = partIndex,
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
            cursor = cursor,
            partSize = partSize
        ).map { it.toDomain() }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun uploadVideo(video: VideoCreationModel): Result<PendingVideo, Error> {
        return request(
            httpExceptionHandler(400) {
                val json = it.response()?.errorBody()!!.string()
                val response = gson.fromJson(json, VideoUploadingErrorResponse::class.java)
                VideoUploadingError(
                    titleError = response.titleError,
                    sourceError = response.sourceError,
                    coverError = response.coverError,
                    descriptionError = response.descriptionError
                )
            }
        ) {
            val coverPart = video.cover?.let {
                fileProvider.uriToPart(it, "cover")
            }
            val sourceFileName = video.metaData.uri.let {
                val mimeType = fileProvider.getFileMimeType(video.metaData.uri)!!
                val extension = MimeTypeMap
                    .getSingleton()
                    .getExtensionFromMimeType(mimeType)
                "source.$extension"
            }
            videoApi.uploadVideoDetails(
                video = VideoUploadingRequest(
                    title = video.title,
                    channelId = video.channelId,
                    description = video.description
                ),
                source = VideoMetaData(
                    fileName = sourceFileName,
                    mimeType = video.metaData.mimeType,
                    size = video.metaData.size
                ),
                cover = coverPart
            ).let {
                PendingVideo(tmpId = Uuid.parse(it.tmpId))
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalAtomicApi::class)
    override suspend fun uploadVideoSource(
        tmpId: Uuid,
        source: String,
        onProgress: (Float) -> Unit
    ): Result<Unit, Error> {
        val coroutineScope = CoroutineScope(Dispatchers.IO.limitedParallelism(4) + SupervisorJob())
        return try {
            val sourceInputStream = fileProvider.getFileAsInputStream(source)!!
            val sourceSize = fileProvider.getFileSize(source)!!
            val bytesTransferred = AtomicLong(0)
            coroutineScope.async {
                val uploadJobs = mutableListOf<Job>()
                val buffer = ByteArray(BUFFER_SIZE)
                var cursor = 0L
                do {
                    val bytesRead = sourceInputStream.read(buffer)
                    if (bytesRead <= 0) {
                        break
                    }
                    val start = cursor
                    val end = start + bytesRead - 1
                    val bytesToSend = buffer.copyOf(bytesRead)
                    uploadJobs += launch {
                        bytesTransferred += bytesRead.toLong()
                        videoApi.uploadVideoSource(
                            tmpId = tmpId,
                            contentRange = "bytes $start-$end/$sourceSize",
                            source = bytesToSend.toRequestBody(bytesNumber = bytesRead)
                        )
                        val progress = bytesTransferred.load().toFloat() / sourceSize
                        onProgress(progress)
                    }
                    cursor = end + 1
                } while (true)
                uploadJobs.joinAll()
                Result.Success(Unit)
            }.await()
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
        } finally {
            coroutineScope.cancel()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun confirmVideoUpload(tmpId: Uuid): Result<Unit, Error> = request(
        httpExceptionHandler(400) {
            val json = it.response()?.body() as String
            val response = gson.fromJson(json, VideoUploadingErrorResponse::class.java)
            VideoUploadingError(
                titleError = response.titleError,
                sourceError = response.sourceError,
                coverError = response.coverError,
                descriptionError = response.descriptionError
            )
        }
    ) {
        videoApi.confirmVideoUpload(tmpId)
    }

    override fun getSourceUrl(videoId: Long): String {
        return "$API_BASE_URL/videos/$videoId/source"
    }

    override fun getCoverUrl(videoId: Long, size: ImageSize): String {
        return "$API_BASE_URL/videos/$videoId/cover?size=${size.name.lowercase()}"
    }

    override suspend fun incrementViews(videoId: Long): Result<Video, Error> = request {
        videoApi.incrementViews(videoId).toDomain()
    }

    override suspend fun deleteVideo(videoId: Long): Result<Unit, Error> = request {
        videoApi.deleteVideo(videoId)
    }

    override suspend fun editVideo(video: VideoEditingModel): Result<Video, Error> {
        return request(
            httpExceptionHandler(400) {
                val json = it.response()!!.errorBody()!!.string()
                val response = gson.fromJson(json, VideoEditingErrorResponse::class.java)
                VideoEditingError(
                    titleError = response.titleError,
                    coverError = response.coverError,
                    descriptionError = response.descriptionError
                )
            }
        ) {
            val coverPart = when (video.cover) {
                is EditingAction.Edit -> fileProvider.uriToPart(video.cover.value, "cover")
                else -> null
            }
            videoApi.editVideo(
                video = VideoEditingRequest(
                    videoId = video.videoId,
                    title = video.title,
                    description = video.description,
                    coverAction = when (video.cover) {
                        is EditingAction.Edit -> EditAction.EDIT
                        EditingAction.Keep -> EditAction.KEEP
                        EditingAction.Remove -> EditAction.REMOVE
                    }
                ),
                cover = coverPart
            ).toDomain()
        }
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

    private companion object {
        const val BUFFER_SIZE = 10 * 1024 * 1024
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
    val description: String?,
    val coverAction: EditAction
)