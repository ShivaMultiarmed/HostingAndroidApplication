package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError

data class VideoEditingErrorResponse(
    val titleError: TextError?,
    val descriptionError: TextError?,
    val coverError: FileError?
)