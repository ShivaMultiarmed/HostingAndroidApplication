package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.OptionError
import mikhail.shell.video.hosting.domain.errors.TextError

data class VideoUploadingErrorResponse(
    val titleError: TextError?,
    val channelIdError: OptionError?,
    val descriptionError: TextError?,
    val sourceError: FileError?,
    val coverError: FileError?
)