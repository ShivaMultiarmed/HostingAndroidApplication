package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

data class SearchVideosScreenState(
    val query: String = "",
    val queryError: TextError? = null,
    val videos: List<VideoWithChannelUi>? = null,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: Error? = null
)