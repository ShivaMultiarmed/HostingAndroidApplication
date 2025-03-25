package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.CreateCommentError
import mikhail.shell.video.hosting.domain.errors.GetCommentsError
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.Result
import java.time.LocalDateTime

interface CommentRepository {
    suspend fun send(comment: Comment): Result<Unit, CompoundError<CreateCommentError>>
    suspend fun getPart(before: LocalDateTime, videoId: Long): Result<List<CommentWithUser>, GetCommentsError>
}