package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationScreenEvent as ScreenEvent

sealed class SignUpConfirmationScreenEvent {
    data class Success(val authModel: AuthModel): ScreenEvent()
    data class Failure(val error: Error): ScreenEvent()
}