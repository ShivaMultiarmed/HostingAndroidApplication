package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

data class RecommendationsScreenState(
    val videos: List<VideoWithChannelUi>? = null,
    val error: Error? = null,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val nextPartIndex: Long = 0,
    val hasMore: Boolean = true
)
