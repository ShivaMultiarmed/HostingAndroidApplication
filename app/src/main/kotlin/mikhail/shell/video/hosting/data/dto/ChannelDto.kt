package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.Subscription

data class ChannelDto(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String? = null,
    val description: String? = null,
    val subscribers: Long = 0,
    val header: String? = null,
    val logo: String? = null
)

fun Channel.toDto() = ChannelDto(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers,
    header = header,
    logo = logo
)

fun ChannelDto.toDomain() = Channel(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers,
    header = header,
    logo = logo
)

data class ChannelWithUserDto(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String? = null,
    val description: String? = null,
    val subscribers: Long = 0,
    val subscription: Subscription = Subscription.NOT_SUBSCRIBED,
    val header: String? = null,
    val logo: String? = null
)

fun ChannelForUser.toDto() = ChannelWithUserDto(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers,
    subscription = subscription,
    header = header,
    logo = logo
)

fun ChannelWithUserDto.toDomain() = ChannelForUser(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers,
    subscription = subscription,
    header = header,
    logo = logo
)