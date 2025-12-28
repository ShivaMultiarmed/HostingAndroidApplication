package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class SignUpVerificationScreenState(
    val code: FieldState<String, Error> = FieldState(""),
    val isRequestingCode: Boolean = false,
    val isLoading: Boolean = false
)