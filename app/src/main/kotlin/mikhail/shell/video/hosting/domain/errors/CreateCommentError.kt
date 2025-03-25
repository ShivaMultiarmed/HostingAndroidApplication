package mikhail.shell.video.hosting.domain.errors

enum class CreateCommentError: Error {
    TEXT_EMPTY,
    UNEXPECTED
}