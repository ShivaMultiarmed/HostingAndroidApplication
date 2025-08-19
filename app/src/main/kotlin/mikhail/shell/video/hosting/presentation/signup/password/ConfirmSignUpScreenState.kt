package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel

data class ConfirmSignUpScreenState(
    val authModel: AuthModel? = null,
    val isLoading: Boolean = false,
    val error: Error? = null
)
