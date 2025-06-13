package mikhail.shell.video.hosting.domain.errors

enum class UploadVideoError: Error {
    TITLE_EMPTY,
    TITLE_TOO_LARGE,

    CHANNEL_NOT_VALID,

    SOURCE_EMPTY,
    SOURCE_NOT_FOUND,
    SOURCE_TOO_LARGE,
    SOURCE_TYPE_NOT_VALID,
    SOURCE_METADATA_NOT_VALID,

    COVER_NOT_FOUND,
    COVER_TYPE_NOT_VALID,
    COVER_TOO_LARGE,

    UNEXPECTED
}