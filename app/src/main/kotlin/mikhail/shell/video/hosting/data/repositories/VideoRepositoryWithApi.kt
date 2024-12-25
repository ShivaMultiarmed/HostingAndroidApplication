package mikhail.shell.video.hosting.data.repositories

import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import retrofit2.HttpException
import javax.inject.Inject

class VideoRepositoryWithApi @Inject constructor(
    private val videoApi: VideoApi
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
}