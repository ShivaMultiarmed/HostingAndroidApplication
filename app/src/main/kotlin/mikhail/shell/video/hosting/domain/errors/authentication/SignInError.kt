package mikhail.shell.video.hosting.domain.errors.authentication

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError

data class SignInError(
    val userNameError: TextError?,
    val passwordError: TextError?
): Error