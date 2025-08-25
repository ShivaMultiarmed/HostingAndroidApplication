package mikhail.shell.video.hosting.domain.usecases.videos.validation

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.FileError.EMPTY
import mikhail.shell.video.hosting.domain.errors.FileError.LARGE
import mikhail.shell.video.hosting.domain.errors.FileError.NOT_SUPPORTED
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_VIDEO_SIZE
import javax.inject.Inject

class ValidateVideoSource @Inject constructor(
    private val fileProvider: FileProvider
) {
    operator fun invoke(video: String?): Result<Unit, FileError> {
        return if (video == null) {
            Result.Failure(EMPTY)
        } else if (fileProvider.getFileMimeType(video) == null) {
            Result.Failure(NOT_SUPPORTED)
        } else if (fileProvider.getFileSize(video)!! > MAX_VIDEO_SIZE) {
            Result.Failure(LARGE)
        } else {
            Result.Success(Unit)
        }
    }
}