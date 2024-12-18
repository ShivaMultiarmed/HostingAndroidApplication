package mikhail.shell.video.hosting.data.repositories

import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.VideoInfo
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import okio.EOFException
import retrofit2.HttpException
import javax.inject.Inject

class VideoRepositoryWithApi @Inject constructor(
    private val videoApi: VideoApi
) : VideoRepository {
    override suspend fun fetchVideoInfo(videoId: Long): Result<VideoInfo, VideoError> {
        return try {
            Result.Success(videoApi.fetchVideoInfo(videoId))
        } catch (e: HttpException) {
            val error = when(e.code()) {
                404 -> VideoError.NOT_FOUND
                else -> VideoError.UNEXPECTED_ERROR
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(VideoError.UNEXPECTED_ERROR)
        }
    }

    override suspend fun fetchExtendedVideoInfo(
        videoId: Long,
        userId: Long
    ): Result<ExtendedVideoInfo, VideoError> {
        return try {
            Result.Success(videoApi.fetchExtendedVideoInfo(videoId, userId))
        } catch (e: HttpException) {
            val error = when(e.code()) {
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
        liking: Boolean
    ): Result<ExtendedVideoInfo, VideoError> {
        return try {
            Result.Success(videoApi.rateVideo(videoId, userId, liking))
        } catch (e: HttpException) {
            val error = when(e.code()) {
                404 -> VideoError.NOT_FOUND
                else -> VideoError.UNEXPECTED_ERROR
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(VideoError.UNEXPECTED_ERROR)
        }
    }
}