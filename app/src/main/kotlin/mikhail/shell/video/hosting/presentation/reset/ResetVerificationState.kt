package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class ResetVerificationState(
    val code: FieldState<String, TextError> = FieldState(""),
    val isLoading: Boolean = false
)
