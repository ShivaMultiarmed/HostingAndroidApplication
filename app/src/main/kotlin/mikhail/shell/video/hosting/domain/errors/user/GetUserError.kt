package mikhail.shell.video.hosting.domain.errors.user

import mikhail.shell.video.hosting.domain.errors.Error

enum class GetUserError: Error {
    NOT_FOUND,
    UNEXPECTED
}