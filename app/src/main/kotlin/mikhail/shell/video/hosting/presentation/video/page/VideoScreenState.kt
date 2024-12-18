package mikhail.shell.video.hosting.presentation.video.page

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo

data class VideoScreenState(
    val extendedVideoInfo: ExtendedVideoInfo? = null,
    val isLoading: Boolean = true,
    val error: VideoError? = null
)
