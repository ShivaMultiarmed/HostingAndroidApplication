package mikhail.shell.video.hosting.domain.errors

enum class GetCommentsError: Error {
    VIDEO_NOT_FOUND,
    USER_NOT_FOUND,
    UNEXPECTED
}