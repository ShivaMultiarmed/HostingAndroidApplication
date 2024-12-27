package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel

data class SignUpWithPasswordState(
    val authModel: AuthModel? = null,
    val error: CompoundError<SignUpError>? = null,
    val isLoading: Boolean = false
)