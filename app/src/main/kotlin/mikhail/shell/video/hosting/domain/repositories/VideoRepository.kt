package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.errors.VideoLoadingError
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.models.VideoWithChannel

interface VideoRepository {
    suspend fun fetchVideoInfo(
        videoId: Long
    ) : Result<Video, VideoError>
    suspend fun fetchVideoDetails(
        videoId: Long,
        userId: Long
    ): Result<VideoDetails, VideoError>

    suspend fun rateVideo(
        videoId: Long,
        userId: Long,
        liking: LikingState
    ) : Result<Video, VideoError>

    suspend fun fetchChannelVideoList(
        channelId: Long,
        userId: Long,
        partNumber: Long,
        partSize: Int
    ): Result<List<Video>, VideoLoadingError>

    suspend fun fetchVideosWithChannelsByQuery(
        query: String,
        partNumber: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, VideoError>

    suspend fun uploadVideo(
        video: Video,
        source: File,
        cover: File?
    ): Result<Video, CompoundError<UploadVideoError>>

    suspend fun incrementViews(
        videoId: Long
    ): Result<Long, Error>
}