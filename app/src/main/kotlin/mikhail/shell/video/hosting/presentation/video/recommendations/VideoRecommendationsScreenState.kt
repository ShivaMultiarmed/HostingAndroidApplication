package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

data class VideoRecommendationsScreenState(
    val videos: List<VideoWithChannelUi>? = null,
    val nextVideosPartIndex: Long = 0,
    val videosLoadingError: Error? = null,
    val areVideosLoading: Boolean = false,
    val areAllVideosLoaded: Boolean = false
)
