package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.presentation.utils.EditingState

sealed class ChannelEditingScreenAction {
    data class ChangeTitle(val title: String) : ChannelEditingScreenAction()
    data object FocusTitle: ChannelEditingScreenAction()
    data object BlurTitle: ChannelEditingScreenAction()
    data class ChangeAlias(val alias: String) : ChannelEditingScreenAction()
    data object FocusAlias: ChannelEditingScreenAction()
    data object BlurAlias: ChannelEditingScreenAction()
    data class ChangeHeader(val editingState: EditingState) : ChannelEditingScreenAction()
    data class ChangeLogo(val editingState: EditingState) : ChannelEditingScreenAction()
    data class ChangeDescription(val description: String) : ChannelEditingScreenAction()
    data object FocusDescription: ChannelEditingScreenAction()
    data object BlurDescription: ChannelEditingScreenAction()
    data object Submit : ChannelEditingScreenAction()
    data object Cancel: ChannelEditingScreenAction()
    data object Restart: ChannelEditingScreenAction()
}