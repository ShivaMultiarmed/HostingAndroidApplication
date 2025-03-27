package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.CommentError
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class SaveComment @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(comment: Comment): Result<Unit, CommentError> {
        return if (comment.text.length > 200) {
            Result.Failure(CommentError.TEXT_TOO_LARGE)
        } else {
            commentRepository.send(comment)
        }
    }
}