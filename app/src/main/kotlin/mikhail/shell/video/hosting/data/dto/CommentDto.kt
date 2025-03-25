package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import java.time.LocalDateTime

data class CommentDto(
    val commentId: Long? = null,
    val videoId: Long,
    val userId: Long,
    val dateTime: LocalDateTime? = null,
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
