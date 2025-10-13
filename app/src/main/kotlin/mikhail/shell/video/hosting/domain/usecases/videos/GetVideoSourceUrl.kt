package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class GetVideoSourceUrl @Inject constructor(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(videoId: Long): String {
        return videoRepository.getSourceUrl(videoId)
    }
}