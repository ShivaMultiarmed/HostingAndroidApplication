package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError

data class ChannelEditingErrorResponse(
    val title: TextError?,
    val alias: TextError?,
    val description: TextError?,
    val header: FileError?,
    val logo: FileError?
): Error
