package mikhail.shell.video.hosting.domain.errors

data class MetadataError(
    val fileNameError: TextError? = null,
    val sizeError: FileError? = null,
    val mimeTypeError: FileError? = null
): Error
