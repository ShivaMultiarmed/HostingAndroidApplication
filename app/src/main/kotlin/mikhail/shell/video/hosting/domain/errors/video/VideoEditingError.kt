package mikhail.shell.video.hosting.domain.errors.video

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError

data class VideoEditingError(
    val titleError: TextError? = null,
    val coverError: FileError? = null,
    val descriptionError: TextError? = null
): Error