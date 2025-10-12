package mikhail.shell.video.hosting.domain.models

import kotlin.time.Instant

data class Comment(
    val commentId: Long,
    val videoId: Long,
    val userId: Long,
    val dateTime: Instant,
    val text: String
)

data class CommentWithUser(
    val comment: Comment,
    val user: User
)

data class CommentCreationModel(
    val videoId: Long,
    val text: String
)

data class CommentEditingModel(
    val commentId: Long,
    val text: String
)