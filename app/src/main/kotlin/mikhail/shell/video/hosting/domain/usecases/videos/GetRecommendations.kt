package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class GetRecommendations @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        partIndex: Long,
        partSize: Int
    ): Result<List<VideoWithChannel>, Error> =
        videoRepository.fetchVideoRecommendations(
            partIndex = partIndex,
            partSize = partSize
        )
}