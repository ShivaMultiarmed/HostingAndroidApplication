package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class GetExtendedVideoInfo @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(videoId: Long, userId: Long): Result<ExtendedVideoInfo, VideoError> {
        return repository.fetchExtendedVideoInfo(videoId, userId)
    }
}