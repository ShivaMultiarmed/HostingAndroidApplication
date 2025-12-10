package mikhail.shell.video.hosting.presentation.user.screen

sealed class ProfileScreenAction {
    data object RestartProfile : ProfileScreenAction()
    data object LoadNextChannelsPart : ProfileScreenAction()
    data class ChooseChannel(val channelId: Long) : ProfileScreenAction()
    data object OpenSettings : ProfileScreenAction()
    data object PublishVideo : ProfileScreenAction()
    data object CreateChannel : ProfileScreenAction()
    data object SignOut : ProfileScreenAction()
    data object Invite : ProfileScreenAction()
}