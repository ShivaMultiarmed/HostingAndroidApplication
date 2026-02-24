package mikhail.shell.video.hosting.presentation.channel.edit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenState as ScreenState

@Parcelize
sealed class ChannelEditingScreenState : Parcelable {
    @Parcelize
    data object Idle : ScreenState()
    @Parcelize
    data object Starting: ScreenState()
    @Parcelize
    data class Failure(
        val error: @RawValue Error
    ): ScreenState()
    @Parcelize
    data class Editing(
        val channel: ChannelEditingInputState,
        val isLoading: Boolean = false
    ): ScreenState()
}

@Parcelize
data class ChannelEditingInputState(
    val channelId: Long,
    val title: FieldState<String, Error>,
    val alias: FieldState<String, Error>,
    val description: FieldState<String, TextError>,
    val header: FieldState<EditingState<String?>, FileError>,
    val logo: FieldState<EditingState<String?>, FileError>,
) : Parcelable {
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