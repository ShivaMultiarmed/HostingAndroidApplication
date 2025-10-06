package mikhail.shell.video.hosting.domain.errors

data class UserEditingError(
    val nickError: Error?,
    val nameError: TextError?,
    val bioError: TextError?,
    val telError: TextError?,
    val emailError: TextError?,
    val avatarError: FileError?
): Error
