package mikhail.shell.video.hosting.domain.errors

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface Error

@Parcelize
data object UnexpectedError: Error, Parcelable {
    override fun toString() = "UNEXPECTED_ERROR"
}