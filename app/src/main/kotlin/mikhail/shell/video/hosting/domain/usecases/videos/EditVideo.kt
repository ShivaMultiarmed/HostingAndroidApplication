package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.models.VideoEditingModel
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class EditVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(video: VideoEditingModel): Result<Video, Error> {
        return videoRepository.editVideo(video)
    }
}