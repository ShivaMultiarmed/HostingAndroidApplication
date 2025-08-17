package mikhail.shell.video.hosting.data.dto

import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.models.VideoForUser

data class VideoDto(
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

fun Video.toDto() = VideoDto(
    videoId = videoId,
    channelId = channelId,
    title = title,
    dateTime = dateTime,
    views = views,
    likes = likes,
    dislikes = dislikes,
    sourceUrl = sourceUrl,
    coverUrl = cover
)

fun VideoDto.toDomain() = Video(
    videoId = videoId,
    channelId = channelId,
    title = title,
    dateTime = dateTime,
    views = views,
    likes = likes,
    dislikes = dislikes,
    sourceUrl = sourceUrl,
    cover = coverUrl
)

data class VideoWithUserDto(
    val videoId: Long? = null,
    val channelId: Long,
    val title: String,
    val dateTime: Instant? = null,
    val views: Long = 0,
    val likes: Long = 0,
    val liking: Liking = Liking.NONE,
    val dislikes: Long,
    val sourceUrl: String? = null,
    val coverUrl: String? = null
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
    sourceUrl = sourceUrl,
    coverUrl = coverUrl
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
    sourceUrl = sourceUrl,
    coverUrl = coverUrl
)

data class VideoWithChannelDto(
    val video: VideoDto,
    val channel: ChannelDto
)

fun VideoWithChannelDto.toDomain() = VideoWithChannel(
    video = video.toDomain(),
    channel = channel.toDomain()
)