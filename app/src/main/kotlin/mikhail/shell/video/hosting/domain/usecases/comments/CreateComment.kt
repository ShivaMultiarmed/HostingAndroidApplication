package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.CreateCommentError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class CreateComment @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(comment: Comment): Result<Unit, CompoundError<CreateCommentError>> {
        return commentRepository.send(comment)
    }
}