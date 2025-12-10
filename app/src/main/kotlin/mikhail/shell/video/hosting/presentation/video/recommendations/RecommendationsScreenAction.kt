package mikhail.shell.video.hosting.presentation.video.recommendations

sealed class RecommendationsScreenAction {
    data object Restart : RecommendationsScreenAction()
    data object LoadNextPart : RecommendationsScreenAction()
    data class ChooseVideo(val videoId: Long) : RecommendationsScreenAction()
}