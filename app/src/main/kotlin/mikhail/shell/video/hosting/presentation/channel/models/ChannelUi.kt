package mikhail.shell.video.hosting.presentation.channel.models

import androidx.compose.runtime.Immutable
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.Subscription

data class ChannelUi(
    val channelId: Long,
    val logo: String,
    val title: String,
    val alias: String?,
    val subscribers: Long
)

fun Channel.toUi(logo: String = "") = ChannelUi(
    channelId = channelId,
    logo = logo,
    title = title,
    alias = alias,
    subscribers = subscribers
)

@Immutable
data class ChannelForUserUi(
    val channelId: Long,
    val logo: Map<ImageSize, String>,
    val header: Map<ImageSize, String>,
    val title: String,
    val alias: String?,
    val description: String?,
    val subscription: Subscription,
    val subscribers: Long,
    val ownerId: Long
)

fun ChannelForUser.toUi(
    logo: Map<ImageSize, String>,
    header: Map<ImageSize, String>,
) = ChannelForUserUi(
    channelId = channelId,
    logo = logo,
    header = header,
    title = title,
    alias = alias,
    description = description,
    subscription = subscription,
    subscribers = subscribers,
    ownerId = ownerId
)