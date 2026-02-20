package mikhail.shell.video.hosting.presentation.comments.models

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mikhail.shell.video.hosting.domain.models.CommentWithUser

@Immutable
data class CommentUi(
    val userId: Long,
    val commentId: Long,
    val nick: String,
    val avatar: String?,
    val text: String,
    val dateTime: LocalDateTime
)

fun CommentWithUser.toUi(avatar: String) = CommentUi(
    userId = user.userId,
    commentId = comment.commentId,
    nick = user.nick,
    avatar = avatar,
    text = comment.text,
    dateTime = comment.dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
)