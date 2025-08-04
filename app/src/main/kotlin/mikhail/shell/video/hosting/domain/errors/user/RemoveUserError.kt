package mikhail.shell.video.hosting.domain.errors.user

import mikhail.shell.video.hosting.domain.errors.Error

enum class RemoveUserError: Error {
    NOT_FOUND,
    FORBIDDEN,
    UNEXPECTED
}