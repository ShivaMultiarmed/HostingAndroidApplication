package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.data.dto.VideoDetailsDto
import mikhail.shell.video.hosting.data.dto.VideoDto
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.VideoInfo
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoDetails
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoRepository {
    suspend fun fetchVideoInfo(
        videoId: Long
    ) : Result<VideoInfo, VideoError>
    suspend fun fetchVideoDetails(
        videoId: Long,
        userId: Long
    ): Result<VideoDetails, VideoError>

    suspend fun rateVideo(
        videoId: Long,
        userId: Long,
        liking: LikingState
    ) : Result<VideoInfo, VideoError>

    suspend fun fetchChannelVideoList(
        channelId: Long,
        userId: Long,
        partNumber: Long,
        partSize: Int
    ): Result<List<VideoInfo>, VideoError>
}