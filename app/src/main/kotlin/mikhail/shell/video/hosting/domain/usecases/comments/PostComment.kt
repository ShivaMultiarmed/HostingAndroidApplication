package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.CommentCreationModel
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class PostComment @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(comment: CommentCreationModel): Result<CommentWithUser, Error> {
        return commentRepository.post(comment)
    }
}