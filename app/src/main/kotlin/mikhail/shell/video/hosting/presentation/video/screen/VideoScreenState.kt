package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.VideoDetails

data class VideoScreenState(
    val videoDetails: VideoDetails? = null,
    val isLoading: Boolean = true,
    val isViewed: Boolean = false,
    val error: VideoError? = null
)
