package mikhail.shell.video.hosting.domain.errors.network

import mikhail.shell.video.hosting.domain.errors.Error

enum class NetworkError: Error {
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