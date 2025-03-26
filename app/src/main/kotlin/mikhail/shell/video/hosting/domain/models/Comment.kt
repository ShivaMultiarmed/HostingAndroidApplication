package mikhail.shell.video.hosting.domain.models

import kotlinx.datetime.Instant

data class Comment(
    val commentId: Long? = null,
    val videoId: Long,
    val userId: Long,
    val dateTime: Instant? = null,
    val text: String
)

data class CommentWithUser(
    val comment: Comment,
    val user: User
)