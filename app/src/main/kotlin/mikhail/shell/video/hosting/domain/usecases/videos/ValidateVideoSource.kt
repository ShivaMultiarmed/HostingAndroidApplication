package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateVideoSource @Inject constructor(
    private val fileProvider: FileProvider
) {
    operator fun invoke(video: String?): Result<Unit, FileError> {
        return if (video == null) {
            Result.Failure(FileError.EMPTY)
        } else if (fileProvider.getFileMimeType(video) == null) {
            Result.Failure(FileError.NOT_SUPPORTED)
        } else if (fileProvider.getFileSize(video)!! > ValidationRules.MAX_VIDEO_SIZE) {
            Result.Failure(FileError.LARGE)
        } else {
            Result.Success(Unit)
        }
    }
}