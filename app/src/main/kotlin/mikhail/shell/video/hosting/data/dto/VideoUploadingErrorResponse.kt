package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.OptionError
import mikhail.shell.video.hosting.domain.errors.TextError

data class VideoUploadingErrorResponse(
    val title: TextError?,
    val channelId: OptionError?,
    val description: TextError?,
    val cover: FileError?,
    val metaData: VideoMetaDataErrorResponse
)

data class VideoMetaDataErrorResponse(
    val fileNameError: TextError?,
    val sizeError: Error? // TODO: some new subtype of Error
)