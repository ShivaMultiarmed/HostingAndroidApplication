package mikhail.shell.video.hosting.domain.usecases.videos

import android.webkit.MimeTypeMap
import mikhail.shell.video.hosting.data.repositories.VideoMetaData
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class UploadVideo @Inject constructor(
    private val videoRepository: VideoRepository,
    private val fileProvider: FileProvider
) {
    suspend operator fun invoke(
        video: Video,
        cover: String?,
        source: String
    ): Result<Video, Error> {
        val fileMetaData = fileProvider.getFile(source) ?: return Result.Failure(FileError.EMPTY)
        return videoRepository.uploadVideo(
            video = video,
            videoMetaData = VideoMetaData(
                fileName = "source." + (
                        MimeTypeMap
                            .getSingleton()
                            .getExtensionFromMimeType(fileMetaData.mimeType!!)
                            ?: return Result.Failure(FileError.NOT_SUPPORTED)
                        ),
                mimeType = fileMetaData.mimeType,
                size = fileMetaData.size!!
            ),
            cover = cover
        )
    }
}