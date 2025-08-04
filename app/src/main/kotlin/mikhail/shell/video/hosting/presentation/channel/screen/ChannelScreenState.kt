package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Video

data class ChannelScreenState(
    val channel: ChannelWithUser? = null,
    val videos: List<Video>? = null,
    val isChannelLoading: Boolean = false,
    val areVideosLoading: Boolean = false,
    val channelLoadingError: Error? = null,
    val videosLoadingError: Error? = null,
    val areAllVideosLoaded: Boolean = false,
    val nextPartNumber: Long = 0
)
