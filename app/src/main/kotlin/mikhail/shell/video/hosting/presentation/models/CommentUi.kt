package mikhail.shell.video.hosting.presentation.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mikhail.shell.video.hosting.domain.models.CommentWithUser

data class CommentUi(
    val userId: Long,
    val commentId: Long,
    val nick: String,
    val avatar: String?,
    val text: String,
    val dateTime: LocalDateTime
)

fun CommentWithUser.toUi() = CommentUi(
    userId = user.userId!!,
    commentId = comment.commentId!!,
    nick = user.nick,
    avatar = user.avatar,
    text = comment.text,
    dateTime = comment.dateTime!!.toLocalDateTime(TimeZone.currentSystemDefault())
)