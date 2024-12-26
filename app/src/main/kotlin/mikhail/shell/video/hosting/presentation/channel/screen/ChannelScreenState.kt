package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Video

data class ChannelScreenState(
    val channel: ChannelWithUser? = null,
    val videos: List<Video> = listOf(),
    val isLoading: Boolean = false,
    val areVideosLoading: Boolean = false,
    val error: ChannelError? = null
)
