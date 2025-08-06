package mikhail.shell.video.hosting.domain.usecases.comments

import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class GetComments @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(before: Instant, videoId: Long): Result<List<CommentWithUser>, Error> {
        return repository.getPart(before, videoId)
    }
}