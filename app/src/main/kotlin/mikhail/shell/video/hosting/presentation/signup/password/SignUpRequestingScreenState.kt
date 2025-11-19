package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class SignUpRequestingScreenState(
    val userName: FieldState<String, Error> = FieldState(""),
    val isLoading: Boolean = false
)
