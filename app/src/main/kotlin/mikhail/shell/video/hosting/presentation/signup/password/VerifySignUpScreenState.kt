package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error

data class VerifySignUpScreenState(
    val token: String? = null,
    val error: Error? = null
)
