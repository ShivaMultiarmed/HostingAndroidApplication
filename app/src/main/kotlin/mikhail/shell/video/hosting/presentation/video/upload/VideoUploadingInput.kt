package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.OptionError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

data class VideoUploadingInput(
    val title: FieldState<String, Error> = FieldState(""),
    val channelId: FieldState<Long?, OptionError> = FieldState(null),
    val source: FieldState<String?, FileError> = FieldState(null),
    val cover: FieldState<String?, FileError> = FieldState(null),
    val description: FieldState<String, TextError> = FieldState("")
)