package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenEvent as ScreenEvent

sealed class UserEditingScreenEvent {
    data object Cancelled: ScreenEvent()
    data object Removed: ScreenEvent()
    data object Success: ScreenEvent()
    data class Failure(val error: Error): ScreenEvent()
}