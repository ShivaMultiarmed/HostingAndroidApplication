package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import kotlin.time.Instant

data class CommentDto(
    val commentId: Long,
    val videoId: Long,
    val userId: Long,
    val dateTime: Instant,
    val text: String,
)

data class CommentWithUserDto(
    val comment: CommentDto,
    val user: UserDto
)

fun Comment.toDto() = CommentDto(
    commentId = commentId,
    videoId = videoId,
    userId = userId,
    dateTime = dateTime,
    text = text
)
fun CommentDto.toDomain() = Comment(
    commentId = commentId,
    videoId = videoId,
    userId = userId,
    dateTime = dateTime,
    text = text
)
fun CommentWithUser.toDto() = CommentWithUserDto(
    comment = comment.toDto(),
    user = user.toDto()
)
fun CommentWithUserDto.toDomain() = CommentWithUser(
    comment = comment.toDomain(),
    user = user.toDomain()
)
