package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class SignUpInputState(
    val password: FieldState<String, TextError> = FieldState(""),
    val passwordDuplicate: FieldState<String, TextError> = FieldState(""),
    val nick: FieldState<String, Error> = FieldState("")
)