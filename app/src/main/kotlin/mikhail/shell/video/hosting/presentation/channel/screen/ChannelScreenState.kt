package mikhail.shell.video.hosting.presentation.channel.screen

import androidx.compose.runtime.Immutable
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelForUserUi
import mikhail.shell.video.hosting.presentation.video.models.VideoUi

@Immutable
data class ChannelScreenState(
    val userId: Long,
    val channel: ChannelForUserUi? = null,
    val isStarting: Boolean = false,
    val isSubscribing: Boolean = false,
    val isRemoving: Boolean = false,
    val error: Error? = null,
    val videos: VideoListState = VideoListState()
)

@Immutable
data class VideoListState(
    val videos: List<VideoUi>? = null,
    val nextPartIndex: Long = 0,
    val hasMore: Boolean = true,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val error: Error? = null
)