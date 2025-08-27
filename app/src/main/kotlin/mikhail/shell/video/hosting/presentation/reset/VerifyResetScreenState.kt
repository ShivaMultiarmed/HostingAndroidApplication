package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error

sealed class VerifyResetScreenState {
    data class Entering(
        val code: String = "",
        val isLoading: Boolean = false,
        val error: Error? = null
    ): VerifyResetScreenState()
    data class Success(val token: String): VerifyResetScreenState()
    data object Expired: VerifyResetScreenState()
}
