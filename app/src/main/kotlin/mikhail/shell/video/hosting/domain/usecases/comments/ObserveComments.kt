package mikhail.shell.video.hosting.domain.usecases.comments

import kotlinx.coroutines.flow.Flow
import mikhail.shell.video.hosting.domain.ActionModel
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class ObserveComments @Inject constructor(
    private val repository: CommentRepository
) {
    operator fun invoke(videoId: Long): Flow<ActionModel<CommentWithUser>> {
        return repository.startReceiving(videoId)
    }
}