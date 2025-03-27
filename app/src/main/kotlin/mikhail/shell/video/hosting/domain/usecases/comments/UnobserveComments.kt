package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import javax.inject.Inject

class UnobserveComments @Inject constructor(
    private val commentRepository: CommentRepository
) {
    operator fun invoke(videoId: Long) {
        commentRepository.stopReceiving(videoId)
    }
}