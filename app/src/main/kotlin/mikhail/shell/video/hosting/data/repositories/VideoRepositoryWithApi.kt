package mikhail.shell.video.hosting.data.repositories

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
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
    ): Result<List<Video>, VideoError> {
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
                else -> VideoError.UNEXPECTED_ERROR
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(VideoError.UNEXPECTED_ERROR)
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
            val sourcePart = fileToPart("source", source)
            val coverPart = if (cover != null) fileToPart("cover", cover) else null
            Result.Success(
                videoApi.uploadVideo(
                    video.toDto(),
                    sourcePart,
                    coverPart,
                ).toDomain()
            )
        } catch (e: HttpException) {
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<UploadVideoError>>(){}.type
            val compoundError = gson.fromJson<CompoundError<UploadVideoError>>(json, type)
            Result.Failure(compoundError)
        } catch (e: Exception) {
            Result.Failure(DEFAULT_UPLOAD_ERROR)
        }
    }
    private fun fileToPart(partName: String, file: File): MultipartBody.Part {
        val requestBody = RequestBody.create(file.mimeType!!.toMediaTypeOrNull(), file.content!!,0, file.content.size)
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }

    companion object {
        private val DEFAULT_UPLOAD_ERROR = CompoundError(mutableListOf(UploadVideoError.UNEXPECTED))
    }
}