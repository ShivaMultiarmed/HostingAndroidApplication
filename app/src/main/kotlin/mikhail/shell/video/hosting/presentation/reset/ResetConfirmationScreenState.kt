package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class ResetConfirmationScreenState (
    val input: ConfirmResetInputState = ConfirmResetInputState(),
    val isLoading: Boolean = false
)

data class ConfirmResetInputState(
    val password: FieldState<String, TextError> = FieldState(""),
    val passwordDuplicate: FieldState<String, TextError> = FieldState("")
)
