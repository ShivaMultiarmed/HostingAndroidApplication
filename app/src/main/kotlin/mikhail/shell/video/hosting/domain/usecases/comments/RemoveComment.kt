package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.CommentError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class RemoveComment @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(commentId: Long): Result<Unit, CommentError> {
        return commentRepository.remove(commentId)
    }
}
