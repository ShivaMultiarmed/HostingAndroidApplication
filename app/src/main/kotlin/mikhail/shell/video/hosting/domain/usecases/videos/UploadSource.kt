package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class UploadSource @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        uploadId: Long,
        source: String,
        onProgress: (Float) -> Unit
    ): Result<Unit, Error> = videoRepository.uploadVideo(
        uploadId = uploadId,
        source = source,
        onProgress = onProgress
    )
}