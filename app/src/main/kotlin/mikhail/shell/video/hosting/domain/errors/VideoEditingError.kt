package mikhail.shell.video.hosting.domain.errors

enum class VideoEditingError: Error {
    VIDEO_NOT_FOUND,
    TITLE_EMPTY,
    TITLE_TOO_LARGE,
    COVER_NOT_FOUND,
    COVER_TYPE_NOT_VALID,
    COVER_TOO_LARGE,
    UNEXPECTED
}