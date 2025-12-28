package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class ResetVerificationScreenState(
    val code: FieldState<String, TextError> = FieldState(""),
    val isRequestingCode: Boolean = false,
    val isLoading: Boolean = false
)
