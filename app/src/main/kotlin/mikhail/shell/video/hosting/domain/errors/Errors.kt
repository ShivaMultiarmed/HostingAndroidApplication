package mikhail.shell.video.hosting.domain.errors

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TextError: Error, Parcelable {
    EMPTY,
    SHORT,
    LONG,
    EXISTS,
    NOT_EXISTS,
    NOT_CORRECT,
    NOT_VALID,
    PATTERN
}

@Parcelize
enum class FileError: Error, Parcelable {
    NOT_FOUND,
    EMPTY,
    LARGE,
    NOT_SUPPORTED,
    NOT_VALID
}

@Parcelize
enum class NumericError: Error, Parcelable {
    EMPTY,
    LOW,
    HIGH,
    NOT_EXISTS,
    NOT_VALID
}