package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi

sealed class SearchVideosScreenState(open val query: String) {
    data class Loading(override val query: String): SearchVideosScreenState(query)
    data class Success(
        override val query: String,
        val videos: List<VideoWithChannelUi>? = null,
        val isLoading: Boolean = false,
        val hasMore: Boolean = true
    ): SearchVideosScreenState(query)
    data class Failure(
        override val query: String,
        val error: Error
    ): SearchVideosScreenState(query)
}
