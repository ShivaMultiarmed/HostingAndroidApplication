package mikhail.shell.video.hosting.data.repositories

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
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

import okio.BufferedSink

import retrofit2.HttpException

import java.io.File

import javax.inject.Inject

class VideoRepositoryWithApi @Inject constructor(
    private val videoApi: VideoApi,
    private val gson: Gson
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
        } catch (e: Exception) {
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
        source: File,
        cover: File?
    ): Result<Video, CompoundError<UploadVideoError>> {
        return try {
            val sourcePart = source.fileToPart("source")
            val coverPart = cover?.fileToPart("cover")
            Result.Success(
                videoApi.uploadVideo(
                    video.toDto(),
                    sourcePart,
                    coverPart,
                ).toDomain()
            )
        } catch (e: HttpException) {
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<UploadVideoError>>() {}.type
            val compoundError = gson.fromJson<CompoundError<UploadVideoError>>(json, type)
            Result.Failure(compoundError)
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Result.Failure(VideoPatchingError.UNEXPECTED)
        }
    }

    override suspend fun deleteVideo(videoId: Long): Result<Boolean, VideoDeletingError> {
        return try {
            videoApi.deleteVideo(videoId)
            Result.Success(true)
        } catch (e: HttpException) {
            Result.Failure(VideoDeletingError.UNEXPECTED)
        } catch (e: Exception) {
            Result.Failure(VideoDeletingError.UNEXPECTED)
        }
    }

    override suspend fun editVideo(video: Video, coverAction: EditAction, cover: File?): Result<Video, VideoEditingError> {
        return try {
            val coverPart = cover?.fileToPart("cover")
            Result.Success(videoApi.editVideo(video.toDto(), coverAction, coverPart).toDomain())
        } catch (e: HttpException) {
            Result.Failure(VideoEditingError.UNEXPECTED)
        } catch (e: HttpException) {
            Result.Failure(VideoEditingError.UNEXPECTED)
        }
    }

    companion object {
        private val DEFAULT_UPLOAD_ERROR = CompoundError(mutableListOf(UploadVideoError.UNEXPECTED))
    }
}

fun File.fileToPart(partName: String): MultipartBody.Part {
    val requestBody = object : RequestBody() {
        override fun contentType() = partName.toMediaTypeOrNull()

        override fun writeTo(sink: BufferedSink) {
            this@fileToPart.inputStream().use { input ->
                input.copyTo(sink.outputStream())
            }
        }
    }
    return MultipartBody.Part.createFormData(partName, this.name, requestBody)
}

//fun InputStream.streamToPart(
//    partName: String,
//    file: File
//): MultipartBody.Part {
//    val requestBody = object : RequestBody() {
//        private val BUFFER_SIZE = 100 * 1024
//
//        private var size = 0L
//
//        override fun contentLength(): Long {
//            return size
//        }
//
//        override fun contentType(): MediaType? = file.mimeType?.toMediaTypeOrNull()
//
//        override fun writeTo(sink: BufferedSink) {
//            val buffer = ByteArray(BUFFER_SIZE)
//            var bytesRead: Int
//            while (this@streamToPart.read(buffer).also { bytesRead = it } != -1) {
//                sink.write(buffer, 0, bytesRead)
//                size += bytesRead
//            }
//        }
//    }
//
//    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
//}