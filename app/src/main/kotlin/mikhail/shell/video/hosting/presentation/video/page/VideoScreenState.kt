package mikhail.shell.video.hosting.presentation.video.page

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo
import mikhail.shell.video.hosting.presentation.exoplayer.PlaybackState

data class VideoScreenState(
    val extendedVideoInfo: ExtendedVideoInfo? = null,
    val playbackState: PlaybackState = PlaybackState.PAUSED,
    val isLoading: Boolean = true,
    val error: VideoError? = null
)
