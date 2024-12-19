package mikhail.shell.video.hosting.presentation.channel

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.ExtendedChannelInfo

data class ChannelScreenState(
    val info: ExtendedChannelInfo? = null,
    val isLoading: Boolean = false,
    val error: ChannelError? = null
)
