package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.domain.errors.Error

sealed class SearchScreenEvent {
    data class Failure(val error: Error) : SearchScreenEvent()
    data class VideoChosen(val videoId: Long) : SearchScreenEvent()
}