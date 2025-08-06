package mikhail.shell.video.hosting.domain.errors.comment

import mikhail.shell.video.hosting.domain.errors.Error

enum class CommentError: Error {
    TEXT_EMPTY,
    TEXT_TOO_LARGE
}