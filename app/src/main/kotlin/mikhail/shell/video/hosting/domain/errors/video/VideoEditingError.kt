package mikhail.shell.video.hosting.domain.errors.video

import mikhail.shell.video.hosting.domain.errors.Error

enum class VideoEditingError: Error {
    TITLE_EMPTY,
    TITLE_TOO_LARGE,

    COVER_NOT_FOUND,
    COVER_TYPE_NOT_VALID,
    COVER_TOO_LARGE
}