package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoForUser
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import kotlin.time.Instant

data class VideoDto(
    val videoId: Long,
    val channelId: Long,
    val title: String,
    val dateTime: Instant,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val description: String?,
)

fun Video.toDto() = VideoDto(
    videoId = videoId,
    channelId = channelId,
    title = title,
    dateTime = dateTime,
    views = views,
    likes = likes,
    dislikes = dislikes,
    description = description
)

fun VideoDto.toDomain() = Video(
    videoId = videoId,
    channelId = channelId,
    title = title,
    dateTime = dateTime,
    views = views,
    likes = likes,
    dislikes = dislikes,
    description = description
)

data class VideoWithUserDto(
    val videoId: Long,
    val channelId: Long,
    val title: String,
    val dateTime: Instant,
    val views: Long,
    val likes: Long,
    val liking: Liking,
    val dislikes: Long,
    val description: String?
)

fun VideoForUser.toDto() = VideoWithUserDto(
    videoId = videoId,
    channelId = channelId,
    title = title,
    dateTime = dateTime,
    views = views,
    likes = likes,
    liking = liking,
    dislikes = dislikes,
    description = description
)

fun VideoWithUserDto.toDomain() = VideoForUser(
    videoId = videoId,
    channelId = channelId,
    title = title,
    dateTime = dateTime,
    views = views,
    likes = likes,
    dislikes = dislikes,
    liking = liking,
    description = description
)

data class VideoWithChannelDto(
    val video: VideoDto,
    val channel: ChannelDto
)

fun VideoWithChannelDto.toDomain() = VideoWithChannel(
    video = video.toDomain(),
    channel = channel.toDomain()
)