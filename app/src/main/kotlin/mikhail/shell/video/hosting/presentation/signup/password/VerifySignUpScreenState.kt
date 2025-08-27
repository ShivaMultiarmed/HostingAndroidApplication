package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error

sealed class VerifySignUpScreenState {
    data class Entering(
        val code: String = "",
        val codeError: Error? = null
    ): VerifySignUpScreenState()
    data object Expired: VerifySignUpScreenState()
    data class Success(val token: String): VerifySignUpScreenState()
}