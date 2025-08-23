package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.presentation.models.CommentUi
import mikhail.shell.video.hosting.presentation.video.models.VideoDetailsUi

sealed class VideoScreenState {
    data object Loading: VideoScreenState()
    data class Success(
        val video: VideoDetailsUi,
        val isViewed: Boolean = false,
        val likingError: Error? = null,
        val subscriptionError: Error? = null,
        val commentsState: CommentsState? = null
    ): VideoScreenState()
    data class Failure(val error: Error): VideoScreenState()
    data object Removed: VideoScreenState()
}

data class CommentsState(
    val comments: List<CommentUi>? = null,
    val action: ActionModel<CommentUi>? = null,
    val error: Error? = null
)