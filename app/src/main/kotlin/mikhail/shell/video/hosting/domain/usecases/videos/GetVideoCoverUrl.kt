package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class GetVideoCoverUrl @Inject constructor(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(videoId: Long, size: ImageSize): String {
        return videoRepository.getCoverUrl(videoId, size)
    }
}