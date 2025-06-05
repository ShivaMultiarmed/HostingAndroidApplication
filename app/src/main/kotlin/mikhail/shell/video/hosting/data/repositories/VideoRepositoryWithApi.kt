package mikhail.shell.video.hosting.data.repositories

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.common.net.HttpHeaders
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.errors.VideoDeletingError
import mikhail.shell.video.hosting.domain.errors.VideoEditingError
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.errors.VideoLoadingError
import mikhail.shell.video.hosting.domain.errors.VideoPatchingError
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
import retrofit2.HttpException
import java.io.File
import java.io.InputStream
import javax.inject.Inject

const val TRANSFER_BUFFER_SIZE = 10 * 1024 * 1024

class VideoRepositoryWithApi @Inject constructor(
    private val videoApi: VideoApi,
    private val gson: Gson,
    private val fileProvider: FileProvider
) : VideoRepository {
    override suspend fun fetchVideoInfo(videoId: Long): Result<Video, VideoError> {
        return try {
            Result.Success(videoApi.fetchVideo(videoId).toDomain())
        } catch (e: HttpException) {
            val error = when (e.code()) {
                404 -> VideoError.NOT_FOUND
                else -> VideoError.UNEXPECTED_ERROR
            }
            Result.Failure(error)
        } catch (_: Exception) {
            Result.Failure(VideoError.UNEXPECTED_ERROR)
        }
    }

    override suspend fun fetchVideoDetails(
        videoId: Long,
        userId: Long
    ): Result<VideoDetails, VideoError> {
        return try {
            Result.Success(videoApi.fetchVideoDetails(videoId, userId).toDomain())
        } catch (e: HttpException) {
            val error = when (e.code()) {
                404 -> VideoError.NOT_FOUND
                else -> VideoError.UNEXPECTED_ERROR
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(VideoError.UNEXPECTED_ERROR)
        }
    }

    override suspend fun rateVideo(
        videoId: Long,
        userId: Long,
        liking: LikingState
    ): Result<Video, VideoError> {
        return try {
            Result.Success(videoApi.rateVideo(videoId, userId, liking).toDomain())
        } catch (e: HttpException) {
            val error = when (e.code()) {
                404 -> VideoError.NOT_FOUND
                else -> VideoError.UNEXPECTED_ERROR
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(VideoError.UNEXPECTED_ERROR)
        }
    }

    override suspend fun fetchChannelVideoList(
        channelId: Long,
        userId: Long,
        partNumber: Long,
        partSize: Int
    ): Result<List<Video>, VideoLoadingError> {
        return try {
            Result.Success(
                videoApi.fetchVideoList(
                    channelId,
                    userId,
                    partNumber,
                    partSize
                ).map {
                    it.toDomain()
                }
            )
        } catch (e: HttpException) {
            val error = when (e.code()) {
                403 -> VideoLoadingError.USER_NOT_SPECIFIED
                404 -> VideoLoadingError.CHANNEL_NOT_FOUND
                else -> VideoLoadingError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(VideoLoadingError.UNEXPECTED)
        }
    }

    override suspend fun fetchVideosWithChannelsByQuery(
        query: String,
        partNumber: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, VideoError> {
        return try {
            Result.Success(
                videoApi.fetchVideoListByQuery(
                    query,
                    partNumber,
                    partSize
                ).map {
                    it.toDomain()
                }
            )
        } catch (e: HttpException) {
            val error = when (e.code()) {
                else -> VideoError.UNEXPECTED_ERROR
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(VideoError.UNEXPECTED_ERROR)
        }
    }

    override suspend fun uploadVideo(
        video: Video,
        source: String,
        cover: String?,
        onProgress: (Float) -> Unit
    ): Result<Video, CompoundError<UploadVideoError>> {
        return try {
            val sourceUri = Uri.parse(source)
            val sourceMime = fileProvider.getFileMimeType(sourceUri)
            val sourceExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(sourceMime)
            val sourceSize = fileProvider.getFileSize(sourceUri)!!
            val videoResponse = videoApi.uploadVideoDetails(video.toDto()).toDomain()
            var bytesTransfered = 0
            val sourceInputStream = fileProvider.getFileAsInputStream(sourceUri)
            sourceInputStream!!.process { bytesRead, buffer ->
                videoApi.uploadVideoSource(
                    videoResponse.videoId!!,
                    sourceExtension!!,
                    buffer.toOctetStream(bytesRead)
                )
                bytesTransfered += bytesRead
                val progress = bytesTransfered.toFloat() / sourceSize
                onProgress(progress)
            }
            cover?.let { notNullCover ->
                val coverUri = Uri.parse(notNullCover)
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
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<UploadVideoError>>() {}.type
            val compoundError = gson.fromJson<CompoundError<UploadVideoError>>(json, type)
            Result.Failure(compoundError)
        } catch (e: Exception) {
            Log.e(this::class.toString(), e.stackTraceToString())
            Result.Failure(DEFAULT_UPLOAD_ERROR)
        }
    }

    override suspend fun incrementViews(videoId: Long): Result<Long, Error> {
        return try {
            Result.Success(videoApi.incrementViews(videoId))
        } catch (e: HttpException) {
            val error = when (e.code()) {
                else -> VideoPatchingError.VIEWS_NOT_INCREMENTED
            }
            Result.Failure(error)
        } catch (_: Exception) {
            Result.Failure(VideoPatchingError.UNEXPECTED)
        }
    }

    override suspend fun deleteVideo(videoId: Long): Result<Boolean, VideoDeletingError> {
        return try {
            videoApi.deleteVideo(videoId)
            Result.Success(true)
        } catch (_: HttpException) {
            Result.Failure(VideoDeletingError.UNEXPECTED)
        } catch (_: Exception) {
            Result.Failure(VideoDeletingError.UNEXPECTED)
        }
    }

    override suspend fun editVideo(
        video: Video,
        coverAction: EditAction,
        cover: File?
    ): Result<Video, CompoundError<VideoEditingError>> {
        return try {
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
                return Result.Failure(compoundError)
            }
            val coverPart = cover?.toPart("cover")
            Result.Success(videoApi.editVideo(video.toDto(), coverAction, coverPart).toDomain())
        } catch (e: HttpException) {
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<VideoEditingError>>() {}.type
            val compoundError = gson.fromJson<CompoundError<VideoEditingError>>(json, type)
            Result.Failure(compoundError)
        } catch (_: Exception) {
            val compoundError = CompoundError<VideoEditingError>(VideoEditingError.UNEXPECTED)
            Result.Failure(compoundError)
        }
    }

    override suspend fun downloadVideo(
        videoId: Long,
        onPartitionLoaded: (mime: String, fileSize: Long, bytes: Array<Byte>) -> Unit
    ): Result<Boolean, VideoLoadingError> {
        val range = 1024 * 1024 * 10
        var start = 0
        var end = start + range - 1
        var size: Long? = null
        var mime: String? = null
        do {
            var response = videoApi.playVideo(
                    videoId,
                    "bytes=$start-$end"
                )
            if (!response.isSuccessful) {
                if (response.code() == 416) { // range not satisfiable - end of file is passed
                    response = videoApi.downloadVideo(
                        videoId,
                        "bytes=$start-"
                    )
                    if (response.body() == null) {
                        return Result.Failure(VideoLoadingError.UNEXPECTED)
                    }
                } else {
                    return Result.Failure(VideoLoadingError.UNEXPECTED)
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
        } while (start < size!!)
        return Result.Success(true)
    }

    companion object {
        private val DEFAULT_UPLOAD_ERROR = CompoundError(mutableListOf(UploadVideoError.UNEXPECTED))
    }
}

fun FileProvider.uriToPart(uriStr: String, partName: String): MultipartBody.Part {
    val uri = Uri.parse(uriStr)
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