package mikhail.shell.video.hosting.domain.errors.video

import mikhail.shell.video.hosting.domain.errors.Error

enum class VideoLoadingError: Error {
    VIDEO_NOT_FOUND,
    CHANNEL_NOT_FOUND,
    USER_NOT_FOUND,
    UNEXPECTED
}
enum class VideoRecommendationsLoadingError: Error {
    UNEXPECTED
}