package mikhail.shell.video.hosting.domain.errors

enum class ChannelCreationError: Error {
    TITLE_EXISTS,
    TITLE_EMPTY,
    TITLE_TOO_LARGE,

    ALIAS_TOO_LARGE,
    ALIAS_EXISTS,

    DESCRIPTION_TOO_LARGE,

    COVER_NOT_FOUND,
    COVER_TYPE_NOT_VALID,
    COVER_TOO_LARGE,

    AVATAR_NOT_FOUND,
    AVATAR_TYPE_NOT_VALID,
    AVATAR_TOO_LARGE,

    UNEXPECTED
}