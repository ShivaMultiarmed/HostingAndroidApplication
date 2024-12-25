package mikhail.shell.video.hosting.presentation.channel

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Video

data class ChannelScreenState(
    val channel: Channel? = null,
    val videos: List<Video> = listOf(),
    val isLoading: Boolean = false,
    val areVideosLoading: Boolean = false,
    val error: ChannelError? = null
)
