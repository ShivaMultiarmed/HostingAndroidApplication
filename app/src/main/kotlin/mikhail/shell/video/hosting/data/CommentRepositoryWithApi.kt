package mikhail.shell.video.hosting.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.CommentApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.CreateCommentError
import mikhail.shell.video.hosting.domain.errors.GetCommentsError
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import retrofit2.HttpException
import java.time.LocalDateTime
import javax.inject.Inject

class CommentRepositoryWithApi @Inject constructor(
    private val commentApi: CommentApi,
    private val gson: Gson
): CommentRepository {
    override suspend fun send(comment: Comment): Result<Unit, CompoundError<CreateCommentError>> {
        return try {
            val commentDto = comment.toDto()
            commentApi.postComment(commentDto)
            Result.Success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorType = object : TypeToken<CompoundError<CreateCommentError>>() {}.type
            val compoundError = gson.fromJson<CompoundError<CreateCommentError>>(errorBody, errorType)
            Result.Failure(compoundError)
        } catch (e: Exception) {
            val compoundError = CompoundError<CreateCommentError>()
            compoundError.add(CreateCommentError.UNEXPECTED)
            Result.Failure(compoundError)
        }
    }

    override suspend fun getPart(
        before: LocalDateTime,
        videoId: Long
    ): Result<List<CommentWithUser>, GetCommentsError> {
        return try {
            val commentWithUserDtos = commentApi.fetchComments(before, videoId)
            val commentWithUserList = commentWithUserDtos.map { it.toDomain() }
            Result.Success(commentWithUserList)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val error = gson.fromJson(errorBody, GetCommentsError::class.java)
            Result.Failure(error)
        } catch (e: Exception) {
            val error = GetCommentsError.UNEXPECTED
            Result.Failure(error)
        }
    }
}
