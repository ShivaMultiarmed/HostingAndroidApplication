package mikhail.shell.video.hosting.domain.errors.channel

import mikhail.shell.video.hosting.domain.errors.Error

enum class ChannelLoadingError: Error {
    NOT_FOUND,
    USER_NOT_FOUND,
    UNEXPECTED
}