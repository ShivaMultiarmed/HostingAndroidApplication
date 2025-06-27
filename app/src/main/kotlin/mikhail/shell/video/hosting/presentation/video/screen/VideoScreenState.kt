package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.presentation.models.CommentModel

data class VideoScreenState(
    val videoDetails: VideoDetails? = null,
    val isLoading: Boolean = false,
    val error: VideoError? = null,
    val comments: List<CommentModel>? = null,
    val actionComment: ActionModel<CommentModel>? = null,
    val commentError: Error? = null
)
