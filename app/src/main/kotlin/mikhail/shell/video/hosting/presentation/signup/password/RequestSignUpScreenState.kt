package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error

data class RequestSignUpScreenState(
    val userName: String = "",
    val userNameError: Error? = null,
    val isAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: Error? = null
)
