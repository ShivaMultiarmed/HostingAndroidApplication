package mikhail.shell.video.hosting.domain.errors

enum class VideoLoadingError: Error {
    UNEXPECTED, CHANNEL_NOT_FOUND, VIDEO_NOT_FOUND, USER_NOT_SPECIFIED
}