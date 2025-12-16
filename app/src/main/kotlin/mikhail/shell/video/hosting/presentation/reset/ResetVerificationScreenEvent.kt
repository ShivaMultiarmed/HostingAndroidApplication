package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationScreenEvent as ScreenEvent

sealed class ResetVerificationScreenEvent {
    data class Success(val token: String): ScreenEvent()
    data class Failure(val error: Error): ScreenEvent()
}