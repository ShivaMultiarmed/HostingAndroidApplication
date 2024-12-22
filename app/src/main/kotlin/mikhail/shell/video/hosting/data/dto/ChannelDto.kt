package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.ChannelInfo
import mikhail.shell.video.hosting.domain.models.SubscriptionState

data class ChannelDto(
    val channelId: Long,
    val ownerId: Long,
    val title: String,
    val alias: String,
    val description: String,
    val subscribers: Long,
    val subscription: SubscriptionState,
    val coverUrl: String,
    val avatarUrl: String
)

fun ChannelInfo.toDto() = ChannelDto(
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

fun ChannelDto.toDomain() = ChannelInfo(
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