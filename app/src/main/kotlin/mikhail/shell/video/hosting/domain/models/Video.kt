package mikhail.shell.video.hosting.domain.models

import kotlinx.datetime.Instant

data class Video(
    val videoId: Long? = null,
    val channelId: Long,
    val title: String,
    val dateTime: Instant? = null,
    val views: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val sourceUrl: String? = null,
    val coverUrl: String? = null
)

data class VideoWithChannel(
    val video: Video,
    val channel: Channel
)

data class VideoForUser(
    val videoId: Long? = null,
    val channelId: Long,
    val title: String,
    val dateTime: Instant? = null,
    val views: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val liking: Liking = Liking.NONE,
    val sourceUrl: String? = null,
    val coverUrl: String? = null,
)

data class VideoWithChannelForUser(
    val video: VideoForUser,
    val channel: ChannelForUser
)