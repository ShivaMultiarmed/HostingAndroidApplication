package mikhail.shell.video.hosting.domain.errors

enum class SignUpError: Error {
    USERNAME_EMPTY,
    USERNAME_MALFORMED,
    USERNAME_EXISTS,
    USERNAME_TOO_LARGE,

    PASSWORD_EMPTY,
    PASSWORD_NOT_VALID,
    PASSWORDS_NOT_MATCH,

    NICK_EMPTY,
    NICK_TOO_LARGE,
    NICK_EXISTS,

    UNEXPECTED
}