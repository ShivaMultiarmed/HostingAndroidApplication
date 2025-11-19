package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class SignUpVerificationScreenState(
    val code: FieldState<String, Error> = FieldState("")
)

sealed class SignUpVerificationEvent {
    data class Failure(val error: Error): SignUpVerificationEvent()
    data class Success(val token: String): SignUpVerificationEvent()
}