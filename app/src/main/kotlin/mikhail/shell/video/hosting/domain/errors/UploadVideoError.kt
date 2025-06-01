package mikhail.shell.video.hosting.domain.errors

enum class UploadVideoError: Error {
    TITLE_EMPTY, TITLE_TOO_LARGE, CHANNEL_NOT_CHOSEN, SOURCE_EMPTY, SOURCE_TYPE_INVALID, UNEXPECTED, TOO_LARGE_SOURCE
}