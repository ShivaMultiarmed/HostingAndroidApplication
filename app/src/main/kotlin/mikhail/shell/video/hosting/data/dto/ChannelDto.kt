package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.Subscription

data class ChannelDto(
    val channelId: Long,
    val ownerId: Long,
    val title: String,
    val alias: String?,
    val description: String?,
    val subscribers: Long
)

fun Channel.toDto() = ChannelDto(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers
)

fun ChannelDto.toDomain() = Channel(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers
)

data class ChannelWithUserDto(
    val channelId: Long,
    val ownerId: Long,
    val title: String,
    val alias: String?,
    val description: String?,
    val subscribers: Long,
    val subscription: Subscription
)

fun ChannelForUser.toDto() = ChannelWithUserDto(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers,
    subscription = subscription
)

fun ChannelWithUserDto.toDomain() = ChannelForUser(
    channelId = channelId,
    ownerId = ownerId,
    title = title,
    alias = alias,
    description = description,
    subscribers = subscribers,
    subscription = subscription
)