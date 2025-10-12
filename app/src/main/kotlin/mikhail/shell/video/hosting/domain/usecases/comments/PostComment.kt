package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.CommentCreationModel
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class PostComment @Inject constructor(
    private val validateComment: ValidateComment,
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(comment: CommentCreationModel): Result<CommentWithUser, Error> {
        val validationResult = validateComment(comment.text)
        return if (validationResult is Result.Failure) {
            Result.Failure(validationResult.error)
        } else {
            commentRepository.post(comment)
        }
    }
}