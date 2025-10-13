package mikhail.shell.video.hosting.domain.models

import kotlin.time.Instant

data class Video(
    val videoId: Long,
    val channelId: Long,
    val title: String,
    val dateTime: Instant,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val description: String?
)

data class VideoWithChannel(
    val video: Video,
    val channel: Channel
)

data class VideoForUser(
    val videoId: Long,
    val channelId: Long,
    val title: String,
    val dateTime: Instant,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val liking: Liking,
    val description: String?
)

data class VideoWithChannelForUser(
    val video: VideoForUser,
    val channel: ChannelForUser
)

data class VideoCreationModel(
    val channelId: Long,
    val title: String,
    val description: String?,
    val cover: String?,
    val metaData: File
)

data class VideoEditingModel(
    val videoId: Long,
    val title: String,
    val description: String?,
    val cover: String?,
    val coverAction: EditAction
)