package mikhail.shell.video.hosting.domain.errors

enum class EditUserError: Error {
    USER_NOT_FOUND,
    NAME_EMPTY,
    NAME_TOO_LARGE,
    NICK_TOO_LARGE,
    AGE_MALFORMED,
    BIO_TOO_LARGE,
    TEL_MALFORMED,
    EMAIL_MALFORMED,
    UNEXPECTED
}