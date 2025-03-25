package mikhail.shell.video.hosting.presentation.models

data class CommentModel(
    val userId: Long,
    val commentId: Long,
    val name: String,
    val text: String
)
