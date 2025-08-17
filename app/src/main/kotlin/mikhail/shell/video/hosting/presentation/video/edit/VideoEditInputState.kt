package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.models.EditAction

data class VideoEditInputState(
    val title: String,
    val coverAction: EditAction,
    val cover: String?
)