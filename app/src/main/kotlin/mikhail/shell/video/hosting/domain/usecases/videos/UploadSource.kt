package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UploadSource @Inject constructor(
    private val videoRepository: VideoRepository
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        tmpId: Uuid,
        source: String,
        onProgress: (Float) -> Unit
    ): Result<Unit, Error> = videoRepository.uploadVideoSource(
        tmpId = tmpId,
        source = source,
        onProgress = onProgress
    )
}