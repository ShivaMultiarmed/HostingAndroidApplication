package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction

data class UserEditingInputState(
    val nick: String,
    val nickError: Error? = null,
    val name: String,
    val nameError: TextError? = null,
    val avatar: String? = null,
    val avatarError: FileError? = null,
    val avatarAction: EditAction = EditAction.KEEP,
    val bio: String,
    val bioError: TextError? = null,
    val tel: String,
    val telError: TextError? = null,
    val email: String,
    val emailError: TextError? = null
)
