package mikhail.shell.video.hosting.domain.validation

import android.content.Context
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.NetworkError
import mikhail.shell.video.hosting.domain.errors.equivalentTo

fun <T: Error> constructInfoMessage(error: T?, errorMessages: Map<T, String>): String? {
    for ((currentError, message) in errorMessages) {
        if (error.equivalentTo(currentError)) {
            return message
        }
    }
    return null
}

fun Context.constructNetworkErrorMessage(error: NetworkError): String {
    return constructInfoMessage(
        error,
        mapOf(
            NetworkError.CONNECTION_ERROR to getString(R.string.connection_error),
            NetworkError.TIMEOUT_EXCEEDED to getString(R.string.timeout_exceeded),
            NetworkError.SERVER_NOT_AVAILABLE to getString(R.string.server_not_available),
            NetworkError.BAD_REQUEST to getString(R.string.bad_request),
            NetworkError.FORBIDDEN to getString(R.string.forbidden_error),
            NetworkError.SERVER_ERROR to getString(R.string.server_error),
            NetworkError.UNEXPECTED to getString(R.string.unexpected_error)
        )
    )!!
}