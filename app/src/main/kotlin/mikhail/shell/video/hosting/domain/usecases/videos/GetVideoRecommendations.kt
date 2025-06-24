package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoRecommendationsLoadingError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class GetVideoRecommendations @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        userId: Long,
        partIndex: Long,
        partSize: Int
    ): Result<Set<VideoWithChannel>, VideoRecommendationsLoadingError> {
        return videoRepository.fetchVideoRecommendations(userId, partIndex, partSize)
    }
}