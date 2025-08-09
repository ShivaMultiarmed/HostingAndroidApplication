package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel

data class EditChannelScreenState(
    val initialChannel: Channel? = null,
    val editedChannel: Channel? = null,
    val initialChannelError: Error? = null,
    val editedChannelError: Error? = null,
    val isLoading: Boolean = false
)
