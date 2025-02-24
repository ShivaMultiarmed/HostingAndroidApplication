package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoLoadingError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class DownloadVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        videoId: Long,
        onPartitionLoaded: (mime: String, fileSize: Long, bytes: Array<Byte>) -> Unit
    ): Result<Boolean, VideoLoadingError> {
        return videoRepository.downloadVideo(videoId, onPartitionLoaded)
    }
}