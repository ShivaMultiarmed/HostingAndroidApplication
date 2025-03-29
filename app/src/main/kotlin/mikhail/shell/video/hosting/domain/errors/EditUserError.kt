package mikhail.shell.video.hosting.domain.errors

enum class EditUserError: Error {
    USER_NOT_FOUND,
    FORBIDDEN,
    NAME_TOO_LARGE,
    NICK_EMPTY,
    NICK_TOO_LARGE,
    AGE_MALFORMED,
    BIO_TOO_LARGE,
    TEL_MALFORMED,
    EMAIL_MALFORMED,
    EMAIL_TOO_LARGE,
    UNEXPECTED
}
