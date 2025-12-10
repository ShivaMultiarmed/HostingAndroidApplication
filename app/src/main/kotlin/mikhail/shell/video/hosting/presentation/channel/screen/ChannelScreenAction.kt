package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.models.Subscription

sealed class ChannelScreenAction {
    data object RestartChannel : ChannelScreenAction()
    data object RestartVideos : ChannelScreenAction()
    data class Subscribe(val subscription: Subscription) : ChannelScreenAction()
    data class ChooseVideo(val videoId: Long) : ChannelScreenAction()
    data object LoadNextPart : ChannelScreenAction()
    data object Edit : ChannelScreenAction()
    data object Remove : ChannelScreenAction()
}