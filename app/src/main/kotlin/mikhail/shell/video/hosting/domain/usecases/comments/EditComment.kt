package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class EditComment @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(comment: Comment): Result<CommentWithUser, Error> = commentRepository.edit(comment)
}
