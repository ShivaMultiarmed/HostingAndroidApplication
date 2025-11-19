package mikhail.shell.video.hosting.domain.errors.user

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError

data class UserCreationError(
    val nickError: TextError?,
    val passwordError: TextError?
): Error
