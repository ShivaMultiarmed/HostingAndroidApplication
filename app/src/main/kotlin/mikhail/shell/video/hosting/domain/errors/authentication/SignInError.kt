package mikhail.shell.video.hosting.domain.errors.authentication

import mikhail.shell.video.hosting.domain.errors.Error

enum class SignInError: Error {
    USERNAME_EMPTY,
    USERNAME_MALFORMED,
    USERNAME_NOT_FOUND,

    PASSWORD_EMPTY,
    PASSWORD_INCORRECT,

    UNEXPECTED
}