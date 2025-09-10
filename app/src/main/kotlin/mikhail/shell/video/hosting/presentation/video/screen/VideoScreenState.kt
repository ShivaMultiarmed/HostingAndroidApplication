package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.presentation.models.CommentUi
import mikhail.shell.video.hosting.presentation.video.models.VideoDetailsUi

data class VideoScreenState(
    val isStarting: Boolean = false,
    val video: VideoDetailsUi? = null,
    val startingError: Error? = null,
    val isViewed: Boolean = false,
    val viewError: Error? = null,
    val likingError: Error? = null,
    val subscriptionError: Error? = null,
    val isRemoved: Boolean = false,
    val removingError: Error? = null,
    val commentsState: CommentsState = CommentsState()
)

data class CommentsState(
    val comments: List<CommentUi>? = null,
    val action: ActionModel<CommentUi>? = null,
    val isLoading: Boolean = false,
    val error: Error? = null
)