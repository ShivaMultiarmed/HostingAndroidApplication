package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class VideoEditingInputState(
    val title: FieldState<String, TextError>,
    val cover: FieldState<String?, FileError>,
    val coverAction: EditAction = EditAction.KEEP,
    val description: FieldState<String, TextError>
)