package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

data class RecommendationsScreenState(
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val videos: List<VideoWithChannelUi>? = null,
    val nextPartIndex: Long = 0,
    val hasMore: Boolean = true,
    val error: Error? = null
)
