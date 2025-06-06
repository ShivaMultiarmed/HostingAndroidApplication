package mikhail.shell.video.hosting.domain.errors

enum class EditChannelError: Error {
    CHANNEL_NOT_EXIST,

    TITLE_EMPTY,
    TITLE_TOO_LARGE,
    TITLE_EXISTS,

    ALIAS_TOO_LARGE,
    ALIAS_EXISTS,

    DESCRIPTION_TOO_LARGE,

    COVER_TYPE_NOT_VALID,
    COVER_TOO_LARGE,
    COVER_NOT_FOUND,

    AVATAR_TYPE_NOT_VALID,
    AVATAR_TOO_LARGE,
    AVATAR_NOT_FOUND,

    UNEXPECTED
}