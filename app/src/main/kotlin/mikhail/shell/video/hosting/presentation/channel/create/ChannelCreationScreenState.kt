package mikhail.shell.video.hosting.presentation.channel.create

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

@Parcelize
data class ChannelCreationScreenState(
    val channel: ChannelCreationInputState,
    val isLoading: Boolean = false
): Parcelable

@Parcelize
data class ChannelCreationInputState(
    val ownerId: Long,
    val title: FieldState<String, Error> = FieldState(""),
    val alias: FieldState<String, Error> = FieldState(""),
    val logo: FieldState<String?, FileError> = FieldState(null),
    val header: FieldState<String?, FileError> = FieldState(null),
    val description: FieldState<String, TextError> = FieldState("")
): Parcelable