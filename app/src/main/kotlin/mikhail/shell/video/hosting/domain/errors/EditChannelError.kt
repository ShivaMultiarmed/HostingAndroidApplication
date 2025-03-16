package mikhail.shell.video.hosting.domain.errors

enum class EditChannelError: Error {
    CHANNEL_NOT_EXIST,
    TITLE_EMPTY,
    TITLE_TOO_LARGE,
    COVER_TOO_LARGE,
    COVER_NOT_EXIST,
    AVATAR_TOO_LARGE,
    AVATAR_NOT_EXIST,
    UNEXPECTED
}