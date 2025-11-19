package mikhail.shell.video.hosting.domain.errors.user

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError

data class UserEditingError(
    val nickError: Error?,
    val nameError: TextError?,
    val bioError: TextError?,
    val telError: TextError?,
    val emailError: TextError?,
    val avatarError: FileError?
): Error