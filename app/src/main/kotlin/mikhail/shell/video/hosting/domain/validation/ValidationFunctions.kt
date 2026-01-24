package mikhail.shell.video.hosting.domain.validation

import android.content.Context
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.network.NetworkError


fun Context.getNetworkErrorMessage(error: NetworkError): String {
    return when(error) {
        NetworkError.CONNECTION_ERROR -> getString(R.string.connection_error)
        NetworkError.NOT_FOUND -> getString(R.string.not_found_error)
        NetworkError.TIMEOUT_EXCEEDED -> getString(R.string.timeout_exceeded)
        NetworkError.SERVER_NOT_AVAILABLE -> getString(R.string.server_not_available)
        NetworkError.BAD_REQUEST -> getString(R.string.bad_request)
        NetworkError.AUTHENTICATION -> getString(R.string.authentication_required)
        NetworkError.FORBIDDEN -> getString(R.string.forbidden_error)
        NetworkError.SERVER_ERROR -> getString(R.string.server_error)
        NetworkError.CONFLICT -> getString(R.string.conflict_error)
    }
}

fun Context.getStandardErrorMessage(error: Error): String? {
    return when (error) {
        NetworkError.AUTHENTICATION -> null
        is NetworkError -> getNetworkErrorMessage(error)
        else ->  getString(R.string.unexpected_error)
    }
}

