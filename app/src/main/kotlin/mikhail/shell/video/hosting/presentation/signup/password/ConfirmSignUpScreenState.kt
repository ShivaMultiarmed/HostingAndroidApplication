package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState

sealed class ConfirmSignUpScreenState {
    data class Entering(
        val input: SignUpInputState = SignUpInputState(),
        val isLoading: Boolean = false,
        val error: Error? = null
    ): ConfirmSignUpScreenState()
    data class Success(val authModel: AuthModel): ConfirmSignUpScreenState()
    data object Expired: ConfirmSignUpScreenState()
}
