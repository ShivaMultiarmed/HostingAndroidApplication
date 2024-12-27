package mikhail.shell.video.hosting.domain.errors

enum class ChannelError: Error {
    NOT_FOUND, UNEXPECTED
}

enum class ChannelCreationError: Error {
    EXISTS, UNEXPECTED, TITLE_EMPTY, DESCRIPTION_EMPTY
}