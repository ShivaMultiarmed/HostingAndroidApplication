package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class SignUpVerificationScreenState(
    val code: FieldState<String, Error> = FieldState("")
)

sealed class SignUpVerificationScreenEvent {
    data class Failure(val error: Error): SignUpVerificationScreenEvent()
    data class Success(val token: String): SignUpVerificationScreenEvent()
}