package mikhail.shell.video.hosting.presentation.channel.create

sealed class ChannelCreationUiEvent {
    data class TitleChanged(val title: String) : ChannelCreationUiEvent()
    data class AliasChanged(val alias: String) : ChannelCreationUiEvent()
    data class HeaderChanged(val header: String?) : ChannelCreationUiEvent()
    data class LogoChanged(val logo: String?) : ChannelCreationUiEvent()
    data class DescriptionChanged(val description: String) : ChannelCreationUiEvent()
    data object Submit : ChannelCreationUiEvent()
    data object Cancel: ChannelCreationUiEvent()
    data class Success(val channelId: Long): ChannelCreationUiEvent()
    data object AuthenticationRequired: ChannelCreationUiEvent()
}