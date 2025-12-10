package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.FieldState

sealed class ChannelEditingScreenState {
    data object Idle : ChannelEditingScreenState()
    data object Starting: ChannelEditingScreenState()
    data class Failure(val error: Error): ChannelEditingScreenState()
    data class Editing(
        val channel: ChannelEditingInputState,
        val isLoading: Boolean = false
    ): ChannelEditingScreenState()
    data object Success: ChannelEditingScreenState()
}

data class ChannelEditingInputState(
    val channelId: Long,
    val title: FieldState<String, Error>,
    val alias: FieldState<String, Error>,
    val description: FieldState<String, TextError>,
    val header: FieldState<EditingState<String?>, FileError>,
    val logo: FieldState<EditingState<String?>, FileError>,
) {
    companion object {
        fun initialize(
            channelId: Long,
            title: String,
            alias: String,
            description: String,
            header: String,
            logo: String
        ) = ChannelEditingInputState(
            channelId = channelId,
            title = FieldState(title),
            alias = FieldState(alias),
            description = FieldState(description),
            header = FieldState(EditingState.Keeping(header)),
            logo = FieldState(EditingState.Keeping(logo))
        )
    }
}