package mikhail.shell.video.hosting.presentation.video.search

sealed class SearchScreenAction {
    data class ChangeQuery(val query: String) : SearchScreenAction()
    data object Submit : SearchScreenAction()
    data object Restart : SearchScreenAction()
    data object LoadNextPart : SearchScreenAction()
    data class ChooseVideo(val videoId: Long) : SearchScreenAction()
}