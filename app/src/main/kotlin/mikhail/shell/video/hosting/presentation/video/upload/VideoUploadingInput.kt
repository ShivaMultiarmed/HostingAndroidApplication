package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.OptionError
import mikhail.shell.video.hosting.domain.errors.TextError

data class VideoUploadingInput(
    val title: String = "",
    val titleError: TextError? = null,
    val channelId: Long? = null,
    val channelError: OptionError? = null,
    val source: String? = null,
    val sourceError: FileError? = null,
    val cover: String? = null,
    val coverError: FileError? = null,
    val description: String = "",
    val descriptionError: TextError? = null
)