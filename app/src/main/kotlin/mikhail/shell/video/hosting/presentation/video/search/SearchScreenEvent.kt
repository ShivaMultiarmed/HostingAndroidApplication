package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.search.SearchScreenEvent as ScreenEvent

sealed class SearchScreenEvent {
    data class Failure(val error: Error) : ScreenEvent()
    data class VideoChosen(val videoId: Long) : ScreenEvent()
}