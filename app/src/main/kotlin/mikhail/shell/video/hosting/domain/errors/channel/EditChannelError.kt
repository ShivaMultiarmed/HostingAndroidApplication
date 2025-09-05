package mikhail.shell.video.hosting.domain.errors.channel

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError

data class ChannelEditingError(
    val titleError: Error?,
    val aliasError: Error?,
    val descriptionError: TextError?,
    val headerError: FileError?,
    val logoError: FileError?
): Error