package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.AuthError
import mikhail.shell.video.hosting.domain.models.AuthModel

data class SignInWithPasswordState(
    val authModel: AuthModel? = null,
    val error: AuthError? = null,
    val isLoading: Boolean = false
)
