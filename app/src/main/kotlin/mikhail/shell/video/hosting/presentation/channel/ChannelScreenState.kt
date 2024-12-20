package mikhail.shell.video.hosting.presentation.channel

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.ExtendedChannelInfo
import mikhail.shell.video.hosting.domain.models.VideoInfo

data class ChannelScreenState(
    val info: ExtendedChannelInfo? = null,
    val videos: List<VideoInfo> = listOf(),
    val isLoading: Boolean = false,
    val areVideosLoading: Boolean = false,
    val error: ChannelError? = null
)
