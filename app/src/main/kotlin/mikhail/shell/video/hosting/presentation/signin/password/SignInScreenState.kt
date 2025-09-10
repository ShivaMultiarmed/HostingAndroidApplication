package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class SignInScreenState(
    val input: SignInInputState = SignInInputState(),
    val error: Error? = null,
    val isLoading: Boolean = false,
    val authModel: AuthModel? = null
)

data class SignInInputState(
    val userName: FieldState<String, Error> = FieldState(""),
    val password: FieldState<String, Error> = FieldState("")
)