package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class EditVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        video: Video,
        coverAction: EditAction,
        cover: String?
    ): Result<Video, Error> {
        return videoRepository.editVideo(video, coverAction, cover)
    }
}