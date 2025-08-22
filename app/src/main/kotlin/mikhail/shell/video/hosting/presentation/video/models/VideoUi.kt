package mikhail.shell.video.hosting.presentation.video.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.models.VideoWithChannelForUser

data class VideoUi(
    val videoId: Long,
    val coverUrl: String,
    val title: String,
    val views: Long,
    val dateTime: LocalDateTime
)

fun Video.toUi() = VideoUi(
    videoId = videoId!!,
    coverUrl = cover!!,
    title = title,
    views = views,
    dateTime = dateTime?.toLocalDateTime(TimeZone.currentSystemDefault())!!
)

data class VideoWithChannelUi(
    val videoId: Long,
    val videoTitle: String,
    val videoCoverUrl: String,
    val channelAvatarUrl: String,
    val channelId: Long,
    val channelTitle: String,
    val views: Long,
    val dateTime: LocalDateTime
)

fun VideoWithChannel.toUi() = VideoWithChannelUi(
    videoId = video.videoId!!,
    videoTitle = video.title,
    videoCoverUrl = video.cover!!,
    channelAvatarUrl = channel.logo!!,
    channelId = channel.channelId!!,
    views = video.views,
    dateTime = video.dateTime!!.toLocalDateTime(TimeZone.currentSystemDefault()),
    channelTitle = channel.title
)

data class VideoDetailsUi(
    val videoId: Long,
    val channelId: Long,
    val ownerId: Long,
    val videoTitle: String,
    val channelTitle: String,
    val sourceUrl: String,
    val dateTime: LocalDateTime,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val avatarUrl: String,
    val liking: Liking,
    val subscription: Subscription,
    val subscribers: Long
)

fun VideoWithChannelForUser.toUi() = VideoDetailsUi(
    videoId = video.videoId!!,
    channelId = channel.channelId!!,
    ownerId = channel.ownerId,
    videoTitle = video.title,
    channelTitle = channel.title,
    sourceUrl = video.sourceUrl!!,
    dateTime = video.dateTime!!.toLocalDateTime(TimeZone.currentSystemDefault()),
    views = video.views,
    likes = video.likes,
    dislikes = video.dislikes,
    avatarUrl = channel.avatarUrl!!,
    liking = video.liking,
    subscription = channel.subscription,
    subscribers = channel.subscribers
)