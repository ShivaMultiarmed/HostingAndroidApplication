package mikhail.shell.video.hosting.domain.errors

enum class RemoveUserError: Error {
    NOT_FOUND,
    FORBIDDEN,
    UNEXPECTED
}
