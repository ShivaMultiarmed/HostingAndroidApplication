package mikhail.shell.video.hosting.presentation.channel.models

import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.Subscription

data class ChannelUi(
    val channelId: Long,
    val avatarUrl: String,
    val title: String,
    val alias: String?,
    val subscribers: Long
)

fun Channel.toUi() = ChannelUi(
    channelId = channelId!!,
    avatarUrl = logo!!,
    title = title,
    alias = alias,
    subscribers = subscribers
)

data class ChannelForUserUi(
    val channelId: Long,
    val logo: String,
    val headerUrl: String,
    val title: String,
    val alias: String?,
    val description: String?,
    val subscription: Subscription,
    val subscribers: Long,
    val ownerId: Long
)

fun ChannelForUser.toUi() = ChannelForUserUi(
    channelId = channelId!!,
    logo = logo!!,
    headerUrl = header!!,
    title = title,
    alias = alias,
    description = description,
    subscription = subscription,
    subscribers = subscribers,
    ownerId = ownerId
)