package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoForUser
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class RateVideo @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(videoId: Long, liking: Liking): Result<VideoForUser, Error> =
        repository.rateVideo(
            videoId = videoId,
            liking = liking
        )
}