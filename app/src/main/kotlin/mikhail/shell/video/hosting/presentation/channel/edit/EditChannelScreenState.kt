package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.errors.Error

data class EditChannelScreenState(
    val initialChannel: Channel? = null,
    val editedChannel: Channel? = null,
    val error: Error? = null,
    val isLoading: Boolean = false
)
