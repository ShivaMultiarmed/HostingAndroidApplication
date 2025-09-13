package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelForUserUi
import mikhail.shell.video.hosting.presentation.video.models.VideoUi

sealed class ChannelScreenState {
    data object Idle: ChannelScreenState()
    data object Starting: ChannelScreenState()
    data class Success(
        val channel: ChannelForUserUi,
        val videoState: VideoListState
    ): ChannelScreenState()
    data class Failure(val error: Error): ChannelScreenState()
    data object Removed: ChannelScreenState()
}
data class VideoListState(
    val videos: List<VideoUi>? = null,
    val nextPartIndex: Long = 0,
    val hasMore: Boolean = true,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val error: Error? = null
)
