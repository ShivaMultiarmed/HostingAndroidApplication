package mikhail.shell.video.hosting.domain.models

import java.time.LocalDateTime

data class Video(
    val videoId: Long? = null,
    val channelId: Long,
    val title: String,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val views: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val sourceUrl: String? = null,
    val coverUrl: String? = null
)

data class VideoWithUser(
    val videoId: Long? = null,
    val channelId: Long,
    val title: String,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val views: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val liking: LikingState = LikingState.NONE,
    val sourceUrl: String? = null,
    val coverUrl: String? = null,
)

data class VideoWithChannel(
    val video: Video,
    val channel: Channel
)