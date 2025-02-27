package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.errors.VideoDeletingError
import mikhail.shell.video.hosting.domain.errors.VideoEditingError
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.errors.VideoLoadingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import java.io.File

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
        source: String,
        cover: String?,
        onProgress: (Float) -> Unit = {}
    ): Result<Video, CompoundError<UploadVideoError>>

    suspend fun incrementViews(
        videoId: Long
    ): Result<Long, Error>

    suspend fun deleteVideo(
        videoId: Long
    ): Result<Boolean, VideoDeletingError>

    suspend fun editVideo(
        video: Video,
        coverAction: EditAction,
        cover: File?
    ): Result<Video, VideoEditingError>

    suspend fun downloadVideo(
        videoId: Long,
        onPartitionLoaded: (mime: String, fileSize: Long, bytes: Array<Byte>) -> Unit
    ): Result<Boolean, VideoLoadingError>
}