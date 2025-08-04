package mikhail.shell.video.hosting.domain.errors.video

import mikhail.shell.video.hosting.domain.errors.Error

enum class VideoPatchingError: Error {
    UNEXPECTED, VIEWS_NOT_INCREMENTED
}