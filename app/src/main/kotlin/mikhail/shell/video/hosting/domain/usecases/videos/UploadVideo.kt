package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class UploadVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        video: Video,
        source: String,
        cover: String?,
        onVideoCreated: (Video) -> Unit,
        onProgress: (Float) -> Unit
    ): Result<Video, Error> = videoRepository.uploadVideo(
        video = video,
        source = source,
        cover = cover,
        onVideoCreated = onVideoCreated,
        onProgress = onProgress
    )
}