package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.models.CommentUi
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.video.models.VideoDetailsUi

data class VideoScreenState(
    val isStarting: Boolean = false,
    val video: VideoDetailsUi? = null,
    val error: Error? = null,
    val isViewed: Boolean = false,
    val commentsState: CommentsState = CommentsState()
)

data class CommentsState(
    val comments: List<CommentUi>? = null,
    val hasMore: Boolean = true,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val error: Error? = null,
    val comment: CommentInputState = CommentInputState()
)

data class CommentInputState(
    val commentId: Long? = null,
    val text: FieldState<String, TextError> = FieldState("")
)