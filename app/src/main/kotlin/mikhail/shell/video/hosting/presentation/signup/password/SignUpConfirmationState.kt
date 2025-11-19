package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel

data class SignUpConfirmationState(
    val user: SignUpInputState = SignUpInputState(),
    val isLoading: Boolean = false
)

sealed class SignUpConfirmationEvent {
    data class Success(val authModel: AuthModel): SignUpConfirmationEvent()
    data class Failure(val error: Error): SignUpConfirmationEvent()
}