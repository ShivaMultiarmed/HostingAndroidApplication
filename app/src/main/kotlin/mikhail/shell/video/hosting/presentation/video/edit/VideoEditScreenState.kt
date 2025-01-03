package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Video

data class VideoEditScreenState(
    val updatedVideo: Video? = null,
    val initialVideo: Video? = null,
    val error: Error? = null,
    val isLoading: Boolean = false
)