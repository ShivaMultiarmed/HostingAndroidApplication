package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ConfirmVideoUpload @Inject constructor(
    private val videoRepository: VideoRepository
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(tmpId: Uuid): Result<Video, Error> {
        return videoRepository.confirmVideoUpload(tmpId)
    }
}