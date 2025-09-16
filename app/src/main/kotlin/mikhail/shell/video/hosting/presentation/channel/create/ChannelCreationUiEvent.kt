package mikhail.shell.video.hosting.presentation.channel.create

sealed class ChannelCreationUiEvent {
    data class TitleChanged(val title: String) : ChannelCreationUiEvent()
    data object TitleFocused: ChannelCreationUiEvent()
    data object TitleBlurred: ChannelCreationUiEvent()
    data class AliasChanged(val alias: String) : ChannelCreationUiEvent()
    data object AliasFocused: ChannelCreationUiEvent()
    data object AliasBlurred: ChannelCreationUiEvent()
    data class HeaderChanged(val header: String?) : ChannelCreationUiEvent()
    data class LogoChanged(val logo: String?) : ChannelCreationUiEvent()
    data class DescriptionChanged(val description: String) : ChannelCreationUiEvent()
    data object DescriptionFocused: ChannelCreationUiEvent()
    data object DescriptionBlurred: ChannelCreationUiEvent()
    data object Submit : ChannelCreationUiEvent()
    data object Cancel: ChannelCreationUiEvent()
}