package mikhail.shell.video.hosting.data.dto

import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser

data class CommentDto(
    val commentId: Long? = null,
    val videoId: Long,
    val userId: Long,
    val dateTime: Instant? = null,
    val text: String,
)

data class CommentWithUserDto(
    val comment: CommentDto,
    val user: UserDto
)

fun Comment.toDto() = CommentDto(commentId, videoId, userId, dateTime, text)
fun CommentDto.toDomain() = Comment(commentId, videoId, userId, dateTime, text)
fun CommentWithUser.toDto() = CommentWithUserDto(comment.toDto(), user.toDto())
fun CommentWithUserDto.toDomain() = CommentWithUser(comment.toDomain(), user.toDomain())
