package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.models.VideoWithUser
import java.time.LocalDateTime

data class VideoDto(
    val videoId: Long? = null,
    val channelId: Long,
    val title: String,
    val dateTime: LocalDateTime? = null,
    val views: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val sourceUrl: String? = null,
    val coverUrl: String? = null
)

fun Video.toDto() = VideoDto(
    videoId,
    channelId,
    title,
    dateTime,
    views,
    likes,
    dislikes,
    sourceUrl,
    coverUrl
)

fun VideoDto.toDomain() = Video(
    videoId,
    channelId,
    title,
    dateTime,
    views,
    likes,
    dislikes,
    sourceUrl,
    coverUrl
)

data class VideoWithUserDto(
    val videoId: Long? = null,
    val channelId: Long,
    val title: String,
    val dateTime: LocalDateTime? = null,
    val views: Long = 0,
    val likes: Long = 0,
    val liking: LikingState = LikingState.NONE,
    val dislikes: Long,
    val sourceUrl: String? = null,
    val coverUrl: String? = null
)

fun VideoWithUser.toDto() = VideoWithUserDto(
    videoId,
    channelId,
    title,
    dateTime,
    views,
    likes,
    liking,
    dislikes,
    sourceUrl,
    coverUrl
)

fun VideoWithUserDto.toDomain() = VideoWithUser(
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

data class VideoWithChannelDto(
    val video: VideoDto,
    val channel: ChannelDto
)

fun VideoWithChannelDto.toDomain() = VideoWithChannel(
    video.toDomain(),
    channel.toDomain()
)