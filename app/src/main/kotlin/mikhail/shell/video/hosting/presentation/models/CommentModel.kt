package mikhail.shell.video.hosting.presentation.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.domain.models.CommentWithUser

data class CommentModel(
    val userId: Long,
    val commentId: Long,
    val nick: String,
    val avatar: String?,
    val text: String,
    val dateTime: Instant
)

fun CommentWithUser.toModel() = CommentModel(
    userId = user.userId!!,
    commentId = comment.commentId!!,
    nick = user.nick,
    avatar = user.avatar,
    text = comment.text,
    dateTime = comment.dateTime?: Clock.System.now()
)