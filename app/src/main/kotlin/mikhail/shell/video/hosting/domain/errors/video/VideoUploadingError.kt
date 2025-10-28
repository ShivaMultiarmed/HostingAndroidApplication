package mikhail.shell.video.hosting.domain.errors.video

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.NumericError
import mikhail.shell.video.hosting.domain.errors.TextError

data class VideoUploadingError(
    val titleError: TextError? = null,
    val channelError: NumericError? = null,
    val sourceError: FileError? = null,
    val coverError: FileError? = null,
    val descriptionError: TextError? = null
): Error