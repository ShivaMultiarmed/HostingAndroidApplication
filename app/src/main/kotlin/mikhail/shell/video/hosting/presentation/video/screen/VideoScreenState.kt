package mikhail.shell.video.hosting.presentation.video.screen

import androidx.compose.runtime.Immutable
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.comments.models.CommentUi
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.video.models.VideoDetailsUi

@Immutable
data class VideoScreenState(
    val userId: Long,
    val video: VideoDetailsUi? = null,
    val isStarting: Boolean = false,
    val error: Error? = null,
    val isViewed: Boolean = false,
    val isRemoving: Boolean = false,
    val commentsState: CommentsState = CommentsState()
)

@Immutable
data class CommentsState(
    val comments: List<CommentUi>? = null,
    val hasMore: Boolean = true,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false,
    val error: Error? = null,
    val comment: CommentInputState = CommentInputState()
)

@Immutable
data class CommentInputState(
    val commentId: Long? = null,
    val text: FieldState<String, TextError> = FieldState("")
)