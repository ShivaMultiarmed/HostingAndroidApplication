package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject
import kotlin.time.Instant

class GetComments @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(
        before: Instant? = null,
        videoId: Long,
        partSize: Int
    ): Result<List<CommentWithUser>, Error> =
        repository.getPart(before, videoId, partSize)
}