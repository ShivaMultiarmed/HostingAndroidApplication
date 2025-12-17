package mikhail.shell.video.hosting.presentation.video.recommendations

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreenEvent as ScreenEvent

sealed class RecommendationsScreenEvent {
    data class VideoChosen(val videoId: Long): ScreenEvent()
    data class Failure(val error: Error): ScreenEvent()
}