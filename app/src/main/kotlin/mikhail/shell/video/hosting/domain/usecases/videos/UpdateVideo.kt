package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoEditingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class UpdateVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        video: Video,
        coverAction: EditAction,
        cover: File?
    ): Result<Video, VideoEditingError> {
        return videoRepository.editVideo(video, coverAction, cover)
    }
}