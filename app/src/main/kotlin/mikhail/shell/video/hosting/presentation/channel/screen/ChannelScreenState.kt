package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelForUserUi
import mikhail.shell.video.hosting.presentation.video.models.VideoUi

data class ChannelScreenState(
    val channel: ChannelForUserUi? = null,
    val videos: List<VideoUi>? = null,
    val isChannelLoading: Boolean = false,
    val areVideosLoading: Boolean = false,
    val channelLoadingError: Error? = null,
    val videosLoadingError: Error? = null,
    val areAllVideosLoaded: Boolean = false,
    val nextPartNumber: Long = 0
)
