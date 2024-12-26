package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel

data class CreateChannelScreenState(
    val channel: Channel? = null,
    val error: ChannelError? = null,
    val isLoading: Boolean = false
)