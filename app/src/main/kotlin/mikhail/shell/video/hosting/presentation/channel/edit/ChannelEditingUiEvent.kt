package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.models.EditAction

sealed class ChannelEditingUiEvent {
    data class TitleChanged(val title: String) : ChannelEditingUiEvent()
    data object TitleFocused: ChannelEditingUiEvent()
    data object TitleBlurred: ChannelEditingUiEvent()
    data class AliasChanged(val alias: String) : ChannelEditingUiEvent()
    data object AliasFocused: ChannelEditingUiEvent()
    data object AliasBlurred: ChannelEditingUiEvent()
    data class HeaderChanged(val header: String?, val action: EditAction) : ChannelEditingUiEvent()
    data class HeaderExists(val exists: Boolean): ChannelEditingUiEvent()
    data class LogoChanged(val logo: String?, val action: EditAction) : ChannelEditingUiEvent()
    data class LogoExists(val exists: Boolean): ChannelEditingUiEvent()
    data class DescriptionChanged(val description: String) : ChannelEditingUiEvent()
    data object DescriptionFocused: ChannelEditingUiEvent()
    data object DescriptionBlurred: ChannelEditingUiEvent()
    data object Submit : ChannelEditingUiEvent()
    data object Cancel: ChannelEditingUiEvent()
    data object Restart: ChannelEditingUiEvent()
}