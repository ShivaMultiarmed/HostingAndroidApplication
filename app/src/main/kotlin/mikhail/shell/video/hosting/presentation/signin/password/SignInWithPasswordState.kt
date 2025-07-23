package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel

data class SignInWithPasswordState(
    val authModel: AuthModel? = null,
    val error: Error? = null,
    val isLoading: Boolean = false
)
