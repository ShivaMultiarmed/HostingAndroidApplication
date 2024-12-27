package mikhail.shell.video.hosting.domain.errors

enum class UploadVideoError: Error {
    TITLE_EMPTY, CHANNEL_NOT_CHOSEN, SOURCE_EMPTY, SOURCE_TYPE_INVALID, UNEXPECTED
}