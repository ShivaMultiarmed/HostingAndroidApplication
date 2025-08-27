package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.AuthModel

sealed class SignInScreenState {
    data class Entering(
        val input: SignInInputState = SignInInputState(),
        val error: Error? = null,
        val isLoading: Boolean = false
    ): SignInScreenState()
    data class Success(val authModel: AuthModel): SignInScreenState()
}

data class SignInInputState(
    val userName: String = "",
    val userNameError: Error? = null,
    val password: String = "",
    val passwordError: TextError? = null
)