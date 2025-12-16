package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationScreenEvent as ScreenEvent

sealed class ResetConfirmationScreenEvent {
    data class Success(val authModel: AuthModel): ScreenEvent()
    data class Failure(val error: Error): ScreenEvent()
}