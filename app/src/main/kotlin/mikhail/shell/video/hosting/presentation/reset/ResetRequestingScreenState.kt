package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class ResetRequestingScreenState(
    val userName: FieldState<String, Error> = FieldState(""),
    val isLoading: Boolean = false
)