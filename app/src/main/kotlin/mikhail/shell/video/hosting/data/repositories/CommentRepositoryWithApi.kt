package mikhail.shell.video.hosting.data.repositories

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.data.api.CommentApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class CommentRepositoryWithApi @Inject constructor(
    private val commentApi: CommentApi,
    private val fcm: FirebaseMessaging
): CommentRepository {
    private val _commentFlow = MutableSharedFlow<ActionModel<CommentWithUser>>()

    override suspend fun send(comment: Comment): Result<Unit, Error> = request {
        commentApi.save(comment.toDto())
    }

    override suspend fun remove(commentId: Long): Result<Unit, Error> = request {
        commentApi.remove(commentId)
    }

    override suspend fun getPart(before: Instant, videoId: Long): Result<List<CommentWithUser>, Error> = request {
        commentApi
            .fetch(videoId, before)
            .map { it.toDomain() }
    }

    override fun startReceiving(videoId: Long): Flow<ActionModel<CommentWithUser>> {
        val topic = "videos.$videoId.comments"
        fcm.subscribeToTopic(topic)
        return _commentFlow.filter { it.model.comment.videoId == videoId }
    }

    override fun stopReceiving(videoId: Long) {
        val topic = "videos.$videoId.comments"
        fcm.unsubscribeFromTopic(topic)
    }

    override suspend fun receive(actionModel: ActionModel<CommentWithUser>) {
        _commentFlow.emit(actionModel)
    }
}
