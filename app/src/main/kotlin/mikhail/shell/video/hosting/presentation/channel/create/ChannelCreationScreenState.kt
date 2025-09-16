package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class ChannelCreationScreenState(
    val owner: Long,
    val title: FieldState<String, Error> = FieldState(""),
    val alias: FieldState<String, Error> = FieldState(""),
    val logo: FieldState<String?, FileError> = FieldState(null),
    val header: FieldState<String?, FileError> = FieldState(null),
    val description: FieldState<String, TextError> = FieldState(""),
    val channelId: Long? = null,
    val isLoading: Boolean = false,
    val error: Error? = null
)