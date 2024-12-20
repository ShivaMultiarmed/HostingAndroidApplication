package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class GetVideoDetails @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(
        videoId: Long,
        userId: Long
    ): Result<VideoDetails, VideoError> {
        return repository.fetchVideoDetails(videoId, userId)
    }
}