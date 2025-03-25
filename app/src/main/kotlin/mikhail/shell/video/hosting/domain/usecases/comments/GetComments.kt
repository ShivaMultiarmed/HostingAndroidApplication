package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.GetCommentsError
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import java.time.LocalDateTime
import javax.inject.Inject

class GetComments @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(before: LocalDateTime, videoId: Long): Result<List<CommentWithUser>, GetCommentsError> {
        return repository.getPart(before, videoId)
    }
}