package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.errors.TextError

data class SignInErrorResponse(
    val userNameError: TextError?,
    val passwordError: TextError?
)
