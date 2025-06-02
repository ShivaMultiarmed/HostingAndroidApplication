package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.VideoEditingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import java.io.File
import javax.inject.Inject

class UpdateVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        video: Video,
        coverAction: EditAction,
        cover: File?
    ): Result<Video, CompoundError<VideoEditingError>> {
        val compoundError = CompoundError<VideoEditingError>()
        if (video.title.length > ValidationRules.MAX_TITLE_LENGTH) {
            compoundError.add(VideoEditingError.TITLE_TOO_LARGE)
        }
        return if (compoundError.isNotNull()) {
            Result.Failure(compoundError)
        } else {
            videoRepository.editVideo(video, coverAction, cover)
        }
    }
}