package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError

data class SignUpInputState(
    val password: String = "",
    val passwordError: TextError? = null,
    val passwordDuplicate: String = "",
    val passwordDuplicateError: TextError? = null,
    val nick: String = "",
    val nickError: Error? = null
)
