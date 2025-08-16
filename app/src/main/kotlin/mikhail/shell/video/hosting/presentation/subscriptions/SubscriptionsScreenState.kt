package mikhail.shell.video.hosting.presentation.subscriptions

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi

data class SubscriptionsScreenState(
    val channels: List<ChannelUi>? = null,
    val error: Error? = null,
    val isLoading: Boolean = false
)
