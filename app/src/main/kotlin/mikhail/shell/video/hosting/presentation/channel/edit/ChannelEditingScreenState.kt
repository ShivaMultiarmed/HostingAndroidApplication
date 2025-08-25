package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error

sealed class ChannelEditingScreenState {
    data object Loading: ChannelEditingScreenState()
    data class Failure(val error: Error): ChannelEditingScreenState()
    data class Editing(
        val initialChannel: EditableChannelUi,
        val editedChannel: ChannelEditingInputState,
        val error: Error? = null,
        val isLoading: Boolean = false
    ): ChannelEditingScreenState()
    data object Success: ChannelEditingScreenState()
}
