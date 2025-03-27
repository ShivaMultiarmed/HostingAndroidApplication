package mikhail.shell.video.hosting.data

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.data.api.CommentApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.ActionModel
import mikhail.shell.video.hosting.domain.errors.CommentError
import mikhail.shell.video.hosting.domain.errors.GetCommentsError
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import retrofit2.HttpException
import javax.inject.Inject

class CommentRepositoryWithApi @Inject constructor(
    private val commentApi: CommentApi,
    private val fcm: FirebaseMessaging,
    private val gson: Gson
): CommentRepository {
    private val _commentFlow = MutableSharedFlow<ActionModel<CommentWithUser>>()
    override suspend fun send(comment: Comment): Result<Unit, CommentError> {
        return try {
            val commentDto = comment.toDto()
            commentApi.save(commentDto)
            Result.Success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val error = gson.fromJson(errorBody, CommentError::class.java)
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(CommentError.UNEXPECTED)
        }
    }

    override suspend fun remove(commentId: Long): Result<Unit, CommentError> {
        return try {
            commentApi.remove(commentId)
            Result.Success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val error = gson.fromJson(errorBody, CommentError::class.java)
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(CommentError.UNEXPECTED)
        }
    }

    override suspend fun getPart(
        before: Instant,
        videoId: Long
    ): Result<List<CommentWithUser>, GetCommentsError> {
        return try {
            val commentWithUserDtos = commentApi.fetch(videoId, before)
            val commentWithUserList = commentWithUserDtos.map { it.toDomain() }
            Result.Success(commentWithUserList)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val error = gson.fromJson(errorBody, GetCommentsError::class.java)
            Result.Failure(error)
        } catch (e: Exception) {
            Log.e("CommentRepositoryWithApi", e.stackTraceToString())
            val error = GetCommentsError.UNEXPECTED
            Result.Failure(error)
        }
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
