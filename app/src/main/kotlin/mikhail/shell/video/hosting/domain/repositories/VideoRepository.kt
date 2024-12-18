package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.VideoInfo
import mikhail.shell.video.hosting.domain.models.Result

interface VideoRepository {
    suspend fun fetchVideoInfo(
        videoId: Long
    ) : Result<VideoInfo, VideoError>
    suspend fun fetchExtendedVideoInfo(
        videoId: Long,
        userId: Long
    ) : Result<ExtendedVideoInfo, VideoError>
    suspend fun rateVideo(
        videoId: Long,
        userId: Long,
        liking: Boolean
    ) : Result<ExtendedVideoInfo, VideoError>
}