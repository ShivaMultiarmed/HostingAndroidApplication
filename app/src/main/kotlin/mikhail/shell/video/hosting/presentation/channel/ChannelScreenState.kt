package mikhail.shell.video.hosting.presentation.channel

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.ChannelInfo
import mikhail.shell.video.hosting.domain.models.VideoInfo

data class ChannelScreenState(
    val info: ChannelInfo? = null,
    val videos: List<VideoInfo> = listOf(),
    val isLoading: Boolean = false,
    val areVideosLoading: Boolean = false,
    val error: ChannelError? = null
)
