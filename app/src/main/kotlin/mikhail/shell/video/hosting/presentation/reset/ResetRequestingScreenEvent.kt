package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.reset.ResetRequestingScreenEvent as ScreenEvent

sealed class ResetRequestingScreenEvent {
    data class Success(
        val userId: Long,
        val userName: String
    ) : ScreenEvent()
    data class Failure(val error: Error) : ScreenEvent()
    data object NavigateBack : ScreenEvent()
}