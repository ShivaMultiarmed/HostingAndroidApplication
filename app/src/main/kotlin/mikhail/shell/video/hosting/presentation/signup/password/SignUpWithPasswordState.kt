package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel

data class SignUpWithPasswordState(
    val authModel: AuthModel? = null,
    val error: Error? = null,
    val isLoading: Boolean = false
)