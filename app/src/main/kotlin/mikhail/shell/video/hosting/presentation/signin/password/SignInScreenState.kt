package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.utils.FieldState

sealed class SignInScreenState {
    data class Entering(
        val input: SignInInputState = SignInInputState(),
        val error: Error? = null,
        val isLoading: Boolean = false
    ): SignInScreenState()
    data class Success(val authModel: AuthModel): SignInScreenState()
}

data class SignInInputState(
    val userName: FieldState<String, Error> = FieldState(""),
    val password: FieldState<String, Error> = FieldState("")
)