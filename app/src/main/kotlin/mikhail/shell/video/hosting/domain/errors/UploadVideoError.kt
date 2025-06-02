package mikhail.shell.video.hosting.domain.errors

enum class UploadVideoError: Error {
    TITLE_EMPTY,
    TITLE_TOO_LARGE,

    CHANNEL_INVALID,

    SOURCE_EMPTY,
    SOURCE_NOT_FOUND,
    SOURCE_TOO_LARGE,
    SOURCE_TYPE_INVALID,

    COVER_NOT_FOUND,
    COVER_TYPE_INVALID,
    COVER_TOO_LARGE,

    UNEXPECTED
}