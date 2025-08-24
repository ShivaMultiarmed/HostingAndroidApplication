package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction

data class VideoEditingInputState(
    val title: String,
    val titleError: TextError? = null,
    val cover: String? = null,
    val coverError: FileError? = null,
    val coverAction: EditAction = EditAction.KEEP,
    val description: String,
    val descriptionError: TextError? = null
)