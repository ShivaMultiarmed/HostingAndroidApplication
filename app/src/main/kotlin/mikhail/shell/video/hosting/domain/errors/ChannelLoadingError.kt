package mikhail.shell.video.hosting.domain.errors

enum class ChannelLoadingError: Error {
    UNEXPECTED, NOT_FOUND, USER_NOT_SPECIFIED
}