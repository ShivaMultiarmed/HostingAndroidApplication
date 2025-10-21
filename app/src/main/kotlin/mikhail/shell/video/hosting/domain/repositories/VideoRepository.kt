package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoCreationModel
import mikhail.shell.video.hosting.domain.models.VideoEditingModel
import mikhail.shell.video.hosting.domain.models.VideoForUser
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.models.VideoWithChannelForUser

interface VideoRepository {
    suspend fun fetchVideoInfo(videoId: Long) : Result<Video, Error>

    suspend fun fetchVideoDetails(videoId: Long): Result<VideoWithChannelForUser, Error>

    suspend fun rateVideo(videoId: Long, liking: Liking) : Result<VideoForUser, Error>

    suspend fun fetchChannelVideoList(
        channelId: Long,
        partNumber: Long,
        partSize: Int
    ): Result<List<Video>, Error>

    suspend fun fetchVideosWithChannelsByQuery(
        query: String,
        cursor: Long?,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error>

    suspend fun incrementViews(videoId: Long): Result<Video, Error>

    suspend fun deleteVideo(videoId: Long): Result<Unit, Error>

    suspend fun editVideo(video: VideoEditingModel): Result<Video, Error>

    suspend fun downloadVideo(
        videoId: Long,
        onPartitionLoaded: (mime: String, fileSize: Long, bytes: Array<Byte>) -> Unit
    ): Result<Unit, Error>

    suspend fun fetchVideoRecommendations(
        partIndex: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error>

    suspend fun uploadVideo(
        uploadId: Long,
        source: String,
        onProgress: (Float) -> Unit
    ): Result<Unit, Error>

    suspend fun uploadVideo(video: VideoCreationModel): Result<Long, Error>

    fun getCoverUrl(videoId: Long, size: ImageSize): String
    fun getSourceUrl(videoId: Long): String
}