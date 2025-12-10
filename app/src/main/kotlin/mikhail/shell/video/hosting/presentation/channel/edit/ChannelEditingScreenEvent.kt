package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error

sealed class ChannelEditingScreenEvent {
    data object Cancelled : ChannelEditingScreenEvent()
    data class Failure(val error: Error) : ChannelEditingScreenEvent()
    data object Success : ChannelEditingScreenEvent()
}