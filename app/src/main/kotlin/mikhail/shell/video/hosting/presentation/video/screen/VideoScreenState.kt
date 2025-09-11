package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error
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
    val initialComment: CommentUi? = null,
    val currentText: String = "",
    val hasMore: Boolean = true,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val loadingError: Error? = null,
    val actionError: Error? = null
)