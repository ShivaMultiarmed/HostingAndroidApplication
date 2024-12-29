package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class IncrementViews @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(videoId: Long): Result<Long, Error> {
        return videoRepository.incrementViews(videoId)
    }
}