package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.CommentCreationModel
import mikhail.shell.video.hosting.domain.models.CommentEditingModel
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import kotlin.time.Instant

interface CommentRepository {
    suspend fun post(comment: CommentCreationModel): Result<CommentWithUser, Error>
    suspend fun getPart(before: Instant, videoId: Long, partSize: Int): Result<List<CommentWithUser>, Error>
    suspend fun remove(commentId: Long): Result<Unit, Error>
    suspend fun edit(comment: CommentEditingModel): Result<CommentWithUser, Error>
}