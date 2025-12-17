package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreenAction as ScreenAction

sealed class ChannelCreationScreenAction {
    data class ChangeTitle(val title: String) : ScreenAction()
    data object FocusTitle : ScreenAction()
    data object BlurTitle : ScreenAction()
    data class ChangeAlias(val alias: String) : ScreenAction()
    data object FocusAlias : ScreenAction()
    data object BlurAlias : ScreenAction()
    data class ChangeHeader(val header: String?) : ScreenAction()
    data class ChangeLogo(val logo: String?) : ScreenAction()
    data class ChangeDescription(val description: String) : ScreenAction()
    data object FocusDescription : ScreenAction()
    data object BlurDescription : ScreenAction()
    data object Submit : ScreenAction()
    data object Cancel : ScreenAction()
}