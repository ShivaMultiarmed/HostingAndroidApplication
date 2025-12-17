package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenAction as ScreenAction

sealed class ChannelScreenAction {
    data object RestartChannel : ScreenAction()
    data object RestartVideos : ScreenAction()
    data class Subscribe(val subscription: Subscription) : ScreenAction()
    data class ChooseVideo(val videoId: Long) : ScreenAction()
    data object LoadNextPart : ScreenAction()
    data object Edit : ScreenAction()
    data object Remove : ScreenAction()
}