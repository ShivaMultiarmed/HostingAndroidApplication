package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.PendingVideo
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoCreationModel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

class UploadVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(video: VideoCreationModel): Result<PendingVideo, Error> {
        return videoRepository.uploadVideo(video)
    }
}