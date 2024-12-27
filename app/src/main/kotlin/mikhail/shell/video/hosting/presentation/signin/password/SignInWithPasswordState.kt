package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.models.AuthModel

data class SignInWithPasswordState(
    val authModel: AuthModel? = null,
    val error: CompoundError<SignInError>? = null,
    val isLoading: Boolean = false
)
