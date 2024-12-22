package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.VideoInfo
import java.time.LocalDateTime

data class VideoDto(
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

fun VideoInfo.toDto() = VideoDto(
    videoId,
    channelId,
    title,
    dateTime,
    views,
    likes,
    dislikes,
    liking,
    sourceUrl,
    coverUrl
)

fun VideoDto.toDomain() = VideoInfo(
    videoId,
    channelId,
    title,
    dateTime,
    views,
    likes,
    dislikes,
    liking,
    sourceUrl,
    coverUrl
)
