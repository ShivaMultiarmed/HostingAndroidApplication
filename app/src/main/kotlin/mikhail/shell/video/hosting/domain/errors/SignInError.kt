package mikhail.shell.video.hosting.domain.errors

enum class SignInError: Error {
    EMAIL_EMPTY, PASSWORD_EMPTY, EMAIL_INVALID, EMAIL_NOT_FOUND, PASSWORD_INCORRECT, UNEXPECTED
}