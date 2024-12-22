package mikhail.shell.video.hosting.domain.models

import java.time.LocalDateTime

data class VideoInfo(
    val videoId: Long,
    val channelId: Long,
    val title: String,
    val dateTime: LocalDateTime,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val liking: LikingState,
    val sourceUrl: String,
    val coverUrl: String
)