package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class SearchForVideos @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        query: String,
        partNumber: Long = 1,
        partSize: Int = 10
    ): Result<List<VideoWithChannel>, VideoError> {
        return videoRepository.fetchVideosWithChannelsByQuery(
            query,
            partNumber,
            partSize
        )
    }
}