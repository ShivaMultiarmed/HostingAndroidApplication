package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.SubscriptionState

data class ChannelDto(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String,
    val description: String,
    val subscribers: Long,
    val coverUrl: String? = null,
    val avatarUrl: String? = null
)

fun Channel.toDto() = ChannelDto(
    channelId,
    ownerId,
    title,
    alias,
    description,
    subscribers,
    coverUrl,
    avatarUrl
)

fun ChannelDto.toDomain() = Channel(
    channelId,
    ownerId,
    title,
    alias,
    description,
    subscribers,
    coverUrl,
    avatarUrl
)

data class ChannelWithUserDto(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String,
    val description: String,
    val subscribers: Long,
    val subscription: SubscriptionState,
    val coverUrl: String? = null,
    val avatarUrl: String? = null
)

fun ChannelWithUser.toDto() = ChannelWithUserDto(
    channelId,
    ownerId,
    title,
    alias,
    description,
    subscribers,
    subscription,
    coverUrl,
    avatarUrl
)

fun ChannelWithUserDto.toDomain() = ChannelWithUser(
    channelId,
    ownerId,
    title,
    alias,
    description,
    subscribers,
    subscription,
    coverUrl,
    avatarUrl
)