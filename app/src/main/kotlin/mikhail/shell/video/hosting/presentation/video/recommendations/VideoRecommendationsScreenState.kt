package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.domain.errors.VideoRecommendationsLoadingError
import mikhail.shell.video.hosting.domain.models.VideoWithChannel

data class VideoRecommendationsScreenState(
    val videos: List<VideoWithChannel>? = null,
    val nextVideosPartIndex: Long = 0,
    val videosLoadingError: VideoRecommendationsLoadingError? = null,
    val areVideosLoading: Boolean = false,
    val areAllVideosLoaded: Boolean = false
)
