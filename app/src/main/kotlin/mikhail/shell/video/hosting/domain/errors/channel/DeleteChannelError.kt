package mikhail.shell.video.hosting.domain.errors.channel

import mikhail.shell.video.hosting.domain.errors.Error

enum class DeleteChannelError: Error {
    FORBIDDEN, CHANNEL_NOT_EXISTS, UNEXPECTED
}