package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

data class RecommendationsScreenState(
    val videos: List<VideoWithChannelUi>? = null,
    val error: Error? = null,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true
)
