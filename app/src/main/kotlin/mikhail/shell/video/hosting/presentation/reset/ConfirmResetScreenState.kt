package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error

data class ConfirmResetScreenState(
    val isAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: Error? = null
)
