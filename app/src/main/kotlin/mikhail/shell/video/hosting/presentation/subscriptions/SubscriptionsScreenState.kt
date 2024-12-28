package mikhail.shell.video.hosting.presentation.subscriptions

import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.models.Channel

data class SubscriptionsScreenState(
    val channels: List<Channel>? = null,
    val error: ChannelLoadingError? = null,
    val isLoading: Boolean = false
)
