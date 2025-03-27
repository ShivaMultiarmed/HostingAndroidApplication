package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.CreateCommentError
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class CreateComment @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(comment: Comment): Result<Unit, CompoundError<CreateCommentError>> {
        val compoundError = CompoundError<CreateCommentError>()
        if (comment.text.length > 200) {
            compoundError.add(CreateCommentError.TEXT_TOO_LARGE)
        }
        return if (compoundError.isNotNull()) {
            Result.Failure(compoundError)
        } else {
            commentRepository.send(comment)
        }
    }
}