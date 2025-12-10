package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.domain.errors.Error

sealed class RecommendationsScreenEvent {
    data class VideoChosen(val videoId: Long): RecommendationsScreenEvent()
    data class Failure(val error: Error): RecommendationsScreenEvent()
}