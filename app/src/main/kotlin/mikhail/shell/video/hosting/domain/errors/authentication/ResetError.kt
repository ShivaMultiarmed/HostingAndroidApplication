package mikhail.shell.video.hosting.domain.errors.authentication

import mikhail.shell.video.hosting.domain.errors.Error

enum class ResetError: Error {
    USERNAME_NOT_FOUND,
    USERNAME_EMPTY,
    USERNAME_MALFORMED,

    CODE_NOT_CORRECT,
    CODE_NOT_VALID,

    TOKEN_NOT_VALID,

    PASSWORD_EMPTY,
    PASSWORDS_NOT_MATCH,
    PASSWORD_NOT_VALID
}