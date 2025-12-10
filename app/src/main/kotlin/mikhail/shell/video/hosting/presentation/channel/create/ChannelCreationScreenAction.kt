package mikhail.shell.video.hosting.presentation.channel.create

sealed class ChannelCreationScreenAction {
    data class ChangeTitle(val title: String) : ChannelCreationScreenAction()
    data object FocusTitle : ChannelCreationScreenAction()
    data object BlurTitle : ChannelCreationScreenAction()
    data class ChangeAlias(val alias: String) : ChannelCreationScreenAction()
    data object FocusAlias : ChannelCreationScreenAction()
    data object BlurAlias : ChannelCreationScreenAction()
    data class ChangeHeader(val header: String?) : ChannelCreationScreenAction()
    data class ChangeLogo(val logo: String?) : ChannelCreationScreenAction()
    data class ChangeDescription(val description: String) : ChannelCreationScreenAction()
    data object FocusDescription : ChannelCreationScreenAction()
    data object BlurDescription : ChannelCreationScreenAction()
    data object Submit : ChannelCreationScreenAction()
    data object Cancel : ChannelCreationScreenAction()
}