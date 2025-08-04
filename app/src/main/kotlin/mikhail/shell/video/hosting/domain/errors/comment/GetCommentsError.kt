package mikhail.shell.video.hosting.domain.errors.comment

import mikhail.shell.video.hosting.domain.errors.Error

enum class GetCommentsError: Error {
    VIDEO_NOT_FOUND,
    USER_NOT_FOUND,
    UNEXPECTED
}