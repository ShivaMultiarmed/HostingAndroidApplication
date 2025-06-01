package mikhail.shell.video.hosting.domain.errors

enum class VideoEditingError: Error {
    UNEXPECTED, TITLE_EMPTY, TITLE_TOO_LARGE, VIDEO_NOT_FOUND
}