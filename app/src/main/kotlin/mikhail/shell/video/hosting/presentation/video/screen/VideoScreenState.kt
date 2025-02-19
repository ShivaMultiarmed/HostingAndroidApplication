package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.presentation.exoplayer.PlaybackState

data class VideoScreenState(
    val videoDetails: VideoDetails? = null,
    //val playbackState: PlaybackState = PlaybackState.PAUSED,
    val isLoading: Boolean = true,
    val isViewed: Boolean = false,
    val error: VideoError? = null
)
