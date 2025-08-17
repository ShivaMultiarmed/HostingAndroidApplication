package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error

data class EditChannelScreenState(
    val initialChannel: EditedChannelUi? = null,
    val initialChannelError: Error? = null,
    val editChannelSuccess: Boolean = false,
    val editedChannelError: Error? = null,
    val isLoading: Boolean = false
)
