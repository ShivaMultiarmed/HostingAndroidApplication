package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.presentation.models.CommentModel

data class VideoScreenState(
    val videoDetails: VideoDetails? = null,
    val isLoading: Boolean = true,
    val isViewed: Boolean = false,
    val error: VideoError? = null,
    val comments: List<CommentModel>? = null,
    val commentError: Error? = null
)
