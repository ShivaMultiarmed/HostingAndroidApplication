package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction as ScreenAction

sealed class ChannelEditingScreenAction {
    data class ChangeTitle(val title: String) : ScreenAction()
    data object FocusTitle: ScreenAction()
    data object BlurTitle: ScreenAction()
    data class ChangeAlias(val alias: String) : ScreenAction()
    data object FocusAlias: ScreenAction()
    data object BlurAlias: ScreenAction()
    data class ChangeHeader(val editingState: EditingState<String?>) : ScreenAction()
    data class ChangeLogo(val editingState: EditingState<String?>) : ScreenAction()
    data class ChangeDescription(val description: String) : ScreenAction()
    data object FocusDescription: ScreenAction()
    data object BlurDescription: ScreenAction()
    data object Submit : ScreenAction()
    data object Cancel: ScreenAction()
    data object Restart: ScreenAction()
}