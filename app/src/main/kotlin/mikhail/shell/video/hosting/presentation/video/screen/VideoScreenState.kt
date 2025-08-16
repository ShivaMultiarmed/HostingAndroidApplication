package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.presentation.models.CommentUi
import mikhail.shell.video.hosting.presentation.video.models.VideoDetailsUi

data class VideoScreenState(
    val videoDetails: VideoDetailsUi? = null,
    val isLoading: Boolean = false,
    val isViewed: Boolean = false,
    val loadingError: Error? = null,
    val comments: List<CommentUi>? = null,
    val actionComment: ActionModel<CommentUi>? = null,
    val commentError: Error? = null,
    val likingError: Error? = null,
    val subscriptionError: Error? = null
)
