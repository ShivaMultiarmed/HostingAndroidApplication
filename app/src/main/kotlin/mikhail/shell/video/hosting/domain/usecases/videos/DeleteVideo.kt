package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoDeletingError
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import mikhail.shell.video.hosting.domain.models.Result
import javax.inject.Inject

class DeleteVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(videoId: Long): Result<Boolean, VideoDeletingError> {
        return videoRepository.deleteVideo(videoId)
    }
}