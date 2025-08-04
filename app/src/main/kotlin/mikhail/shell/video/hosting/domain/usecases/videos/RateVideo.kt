package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class RateVideo @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(videoId: Long, userId:Long, likingState: LikingState): Result<Video, Error> {
        return repository.rateVideo(videoId, userId, likingState)
    }
}