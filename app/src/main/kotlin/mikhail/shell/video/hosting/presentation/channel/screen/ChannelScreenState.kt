package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelForUserUi
import mikhail.shell.video.hosting.presentation.video.models.VideoUi

sealed class ChannelScreenState {
    data object Loading: ChannelScreenState()
    data class Success(
        val channel: ChannelForUserUi,
        val videoState: VideoListState
    ): ChannelScreenState()
    data class Failure(val error: Error): ChannelScreenState()
    data object Removed: ChannelScreenState()
}
data class VideoListState(
    val videos: List<VideoUi>? = null,
    val hasMore: Boolean = true,
    val isLoading: Boolean = true,
    val error: Error? = null
)
