package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class UserEditingInputState(
    val nick: FieldState<String, Error>,
    val name: FieldState<String, TextError>,
    val avatar: FieldState<String?, FileError> = FieldState(null),
    val avatarAction: EditAction = EditAction.KEEP,
    val bio: FieldState<String, TextError>,
    val tel: FieldState<String, TextError>,
    val email: FieldState<String, TextError>
)
