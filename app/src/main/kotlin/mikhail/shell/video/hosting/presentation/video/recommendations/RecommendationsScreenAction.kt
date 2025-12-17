package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreenAction as ScreenAction

sealed class RecommendationsScreenAction {
    data object Restart : ScreenAction()
    data object LoadNextPart : ScreenAction()
    data class ChooseVideo(val videoId: Long) : ScreenAction()
}