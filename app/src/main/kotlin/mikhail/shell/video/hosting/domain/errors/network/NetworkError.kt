package mikhail.shell.video.hosting.domain.errors.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import mikhail.shell.video.hosting.domain.errors.Error

@Parcelize
enum class NetworkError: Error, Parcelable {
    CONNECTION_ERROR,
    TIMEOUT_EXCEEDED,
    SERVER_NOT_AVAILABLE,
    AUTHENTICATION,
    FORBIDDEN,
    SERVER_ERROR,
    NOT_FOUND,
    BAD_REQUEST,
    CONFLICT
}