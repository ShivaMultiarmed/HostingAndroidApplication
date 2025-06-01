package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject


class UploadVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        video: Video, source: String, cover: String?, onProgress: (Float) -> Unit = {}
    ): Result<Video, CompoundError<UploadVideoError>> {
        val compoundError = CompoundError<UploadVideoError>()
        if (video.title.length > 255) {
            compoundError.add(UploadVideoError.TITLE_TOO_LARGE)
        }
        return if (compoundError.isNotNull()) {
            Result.Failure(compoundError)
        } else {
            videoRepository.uploadVideo(video, source, cover, onProgress)
        }
    }
}