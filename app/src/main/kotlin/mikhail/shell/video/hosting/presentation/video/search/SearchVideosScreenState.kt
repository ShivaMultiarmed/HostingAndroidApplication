package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.VideoWithChannel

data class SearchVideosScreenState(
    val query: String? = null,
    val videos: List<VideoWithChannel>? = null,
    val error: VideoError? = null,
    val isLoading: Boolean = false
)
