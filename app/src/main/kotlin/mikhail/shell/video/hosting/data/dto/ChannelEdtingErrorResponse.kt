package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError

data class ChannelEditingErrorResponse(
    val titleError: TextError?,
    val aliasError: TextError?,
    val descriptionError: TextError?,
    val headerError: FileError?,
    val logoError: FileError?
)
