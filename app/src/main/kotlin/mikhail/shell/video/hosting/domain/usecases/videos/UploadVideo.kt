package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import java.io.File
import javax.inject.Inject


class UploadVideo @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(
        video: Video, source: File, cover: File?, onProgress: (Float) -> Unit = {}
    ): Result<Video,CompoundError<UploadVideoError>> {
        return videoRepository.uploadVideo(video, source, cover, onProgress)
    }
}