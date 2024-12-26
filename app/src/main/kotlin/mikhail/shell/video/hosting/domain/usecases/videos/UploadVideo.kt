package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject


class UploadVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        video: Video, source: File?, cover: File?
    ): Result<Video,VideoError> {
        if (source == null)
            return Result.Failure(VideoError.UNEXPECTED_ERROR)
        return videoRepository.uploadVideo(video, source, cover)
    }
}