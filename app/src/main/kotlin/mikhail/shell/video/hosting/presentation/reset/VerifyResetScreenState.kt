package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error

data class VerifyResetScreenState(
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: Error? = null
)
