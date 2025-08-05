package mikhail.shell.video.hosting.domain.errors.user

import mikhail.shell.video.hosting.domain.errors.Error

enum class EditUserError: Error {
    NICK_EMPTY,
    NICK_TOO_LARGE,

    NAME_TOO_LARGE,

    BIO_TOO_LARGE,

    TEL_MALFORMED,

    EMAIL_MALFORMED,
    EMAIL_TOO_LARGE,

    AVATAR_TOO_LARGE,
    AVATAR_TYPE_NOT_VALID,
}
