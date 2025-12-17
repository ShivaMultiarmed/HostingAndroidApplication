package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenEvent as ScreenEvent

sealed class ChannelEditingScreenEvent {
    data object Cancelled : ScreenEvent()
    data class Failure(val error: Error) : ScreenEvent()
    data object Success : ScreenEvent()
}