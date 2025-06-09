package mikhail.shell.video.hosting.domain.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.domain.errors.CommentError
import mikhail.shell.video.hosting.domain.errors.GetCommentsError
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result

interface CommentRepository {
    suspend fun send(comment: Comment): Result<Unit, CommentError>
    suspend fun getPart(before: Instant, videoId: Long): Result<List<CommentWithUser>, GetCommentsError>
    fun startReceiving(videoId: Long): Flow<ActionModel<CommentWithUser>>
    fun stopReceiving(videoId: Long)
    suspend fun receive(actionModel: ActionModel<CommentWithUser>)
    suspend fun remove(commentId: Long): Result<Unit, CommentError>
}