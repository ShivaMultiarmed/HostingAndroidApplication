package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.Error
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
    ) : Result<Video, Error>

    suspend fun fetchVideoDetails(
        videoId: Long,
        userId: Long
    ): Result<VideoDetails, Error>

    suspend fun rateVideo(
        videoId: Long,
        liking: LikingState
    ) : Result<Video, Error>

    suspend fun fetchChannelVideoList(
        channelId: Long,
        partNumber: Long,
        partSize: Int
    ): Result<List<Video>, Error>

    suspend fun fetchVideosWithChannelsByQuery(
        query: String,
        partNumber: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error>

    suspend fun uploadVideo(
        video: Video,
        source: String,
        cover: String?,
        onProgress: (Float) -> Unit
    ): Result<Video, Error>

    suspend fun incrementViews(
        videoId: Long
    ): Result<Long, Error>

    suspend fun deleteVideo(
        videoId: Long
    ): Result<Unit, Error>

    suspend fun editVideo(
        video: Video,
        coverAction: EditAction,
        cover: File?
    ): Result<Video, Error>

    suspend fun downloadVideo(
        videoId: Long,
        onPartitionLoaded: (mime: String, fileSize: Long, bytes: Array<Byte>) -> Unit
    ): Result<Boolean, Error>

    suspend fun fetchVideoRecommendations(
        partIndex: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error>
}