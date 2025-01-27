package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.models.EditAction
import java.io.File

data class VideoEditInputState(
    val title: String,
    val coverAction: EditAction,
    val cover: File?
)