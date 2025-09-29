package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class ChannelEditingInputState(
    val title: FieldState<String, Error> = FieldState(""),
    val alias: FieldState<String, Error> = FieldState(""),
    val description: FieldState<String, TextError> = FieldState(""),
    val header: FieldState<String?, FileError> = FieldState(null),
    val headerAction: EditAction = EditAction.KEEP,
    val logo: FieldState<String?, FileError> = FieldState(null),
    val logoAction: EditAction = EditAction.KEEP
)
