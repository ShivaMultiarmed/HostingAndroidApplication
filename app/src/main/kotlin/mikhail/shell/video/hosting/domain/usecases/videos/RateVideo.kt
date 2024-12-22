package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoInfo
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class RateVideo @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(videoId: Long, userId:Long, likingState: LikingState): Result<VideoInfo, VideoError> {
        return repository.rateVideo(videoId, userId, likingState)
    }
}