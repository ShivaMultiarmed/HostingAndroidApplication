package mikhail.shell.video.hosting.domain.errors

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetadataError(
    val fileNameError: TextError? = null,
    val sizeError: FileError? = null,
    val mimeTypeError: FileError? = null
): Error, Parcelable
