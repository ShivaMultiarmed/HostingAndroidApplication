package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

data class SearchScreenState(
    val query: String = "",
    val queryError: TextError? = null,
    val videos: List<VideoWithChannelUi>? = null,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val startingError: Error? = null,
    val loadingError: Error? = null
)