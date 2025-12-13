package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenEvent as ScreenEvent

sealed class VideoEditingScreenEvent {
    data object Cancelled : ScreenEvent()
    data class Failure(val error: Error) : ScreenEvent()
    data object Success : ScreenEvent()
}