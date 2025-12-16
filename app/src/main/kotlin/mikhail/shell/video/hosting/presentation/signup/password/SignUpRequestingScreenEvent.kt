package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.signup.password.SignUpRequestingScreenEvent as ScreenEvent

sealed class SignUpRequestingScreenEvent {
    data class Success(val userName: String) : ScreenEvent()
    data class Failure(val error: Error) : ScreenEvent()
    data object Cancel : ScreenEvent()
}