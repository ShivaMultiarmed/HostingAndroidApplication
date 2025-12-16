package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenEvent as ScreenEvent

sealed class SignInScreenEvent {
    data object SignUpRequested : ScreenEvent()
    data object ResetRequested : ScreenEvent()
    data class Failure(val error: Error) : ScreenEvent()
    data class Success(val authModel: AuthModel) : ScreenEvent()
}