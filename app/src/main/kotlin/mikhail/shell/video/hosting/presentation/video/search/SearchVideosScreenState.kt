package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

data class SearchVideosScreenState(
    val query: String? = null,
    val videos: List<VideoWithChannelUi>? = null,
    val error: Error? = null,
    val isLoading: Boolean = false,
    val areAllVideosLoaded: Boolean = false,
    val nextPartNumber: Long = 0
)
