package mikhail.shell.video.hosting.presentation.video.search

import mikhail.shell.video.hosting.presentation.video.search.SearchScreenAction as ScreenAction

sealed class SearchScreenAction {
    data class ChangeQuery(val query: String) : ScreenAction()
    data object Submit : ScreenAction()
    data object Restart : ScreenAction()
    data object LoadNextPart : ScreenAction()
    data class ChooseVideo(val videoId: Long) : ScreenAction()
    data class ChooseChannel(val channelId: Long) : ScreenAction()
}