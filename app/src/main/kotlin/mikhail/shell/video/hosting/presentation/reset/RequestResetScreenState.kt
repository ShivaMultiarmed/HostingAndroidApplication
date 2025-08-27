package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error

sealed class RequestResetScreenState {
    data class Entering(
        val userName: String = "",
        val userNameError: Error? = null,
        val isLoading: Boolean = false,
        val error: Error? = null
    ): RequestResetScreenState()
    data class Success(val userName: String): RequestResetScreenState()
}