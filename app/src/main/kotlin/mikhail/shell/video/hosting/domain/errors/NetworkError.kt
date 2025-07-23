package mikhail.shell.video.hosting.domain.errors

enum class NetworkError: Error {
    CONNECTION_ERROR,
    TIMEOUT_EXCEEDED,
    SERVER_NOT_AVAILABLE,
    BAD_REQUEST,
    FORBIDDEN,
    NOT_FOUND,
    SERVER_ERROR,
    UNEXPECTED
}