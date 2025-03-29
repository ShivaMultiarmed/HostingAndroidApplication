package mikhail.shell.video.hosting.domain.errors

enum class EditUserError: Error {
    USER_NOT_FOUND,
    FORBIDDEN,
    NICK_EMPTY,
    NICK_TOO_LARGE,
    NAME_TOO_LARGE,
    BIO_TOO_LARGE,
    TEL_MALFORMED,
    EMAIL_MALFORMED,
    EMAIL_TOO_LARGE,
    AVATAR_TOO_LARGE,
    AVATAR_MIME_NOT_SUPPORTED,
    UNEXPECTED
}
