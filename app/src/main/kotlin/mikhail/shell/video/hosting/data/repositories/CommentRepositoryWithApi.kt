package mikhail.shell.video.hosting.data.repositories

import mikhail.shell.video.hosting.data.api.CommentApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.CommentCreationModel
import mikhail.shell.video.hosting.domain.models.CommentEditingModel
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject
import kotlin.time.Instant

class CommentRepositoryWithApi @Inject constructor(
    private val commentApi: CommentApi
): CommentRepository {

    override suspend fun post(comment: CommentCreationModel): Result<CommentWithUser, Error> = request {
        commentApi.save(
            CommentCreationRequest(
                videoId = comment.videoId,
                text = comment.text
            )
        ).toDomain()
    }

    override suspend fun edit(comment: CommentEditingModel): Result<CommentWithUser, Error> = request {
        commentApi.edit(
            CommentEditingRequest(
                commentId = comment.commentId,
                text = comment.text
            )
        ).toDomain()
    }

    override suspend fun remove(commentId: Long): Result<Unit, Error> = request {
        commentApi.remove(commentId)
    }

    override suspend fun getPart(before: Instant?, videoId: Long, partSize: Int): Result<List<CommentWithUser>, Error> = request {
        commentApi.fetch(videoId, before, partSize).map { it.toDomain() }
    }
}


data class CommentCreationRequest(
    val videoId: Long,
    val text: String
)

data class CommentEditingRequest(
    val commentId: Long,
    val text: String
)