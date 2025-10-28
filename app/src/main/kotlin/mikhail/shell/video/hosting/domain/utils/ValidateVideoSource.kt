package mikhail.shell.video.hosting.domain.utils

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.ValidationRules.FILE_NAME_REGEX
import javax.inject.Inject

class ValidateVideoSource @Inject constructor(
    private val fileProvider: FileProvider
) {
    operator fun invoke(source: String?): Result<Unit, FileError> {
        return if (source == null || fileProvider.getFileSize(source) == 0L) {
            Result.Failure(FileError.EMPTY)
        } else if (!fileProvider.exists(source)) {
            Result.Failure(FileError.NOT_FOUND)
        } else if (fileProvider.getFileName(source) == null || !fileProvider.getFileName(source)!!.matches(FILE_NAME_REGEX.toRegex())) {
            Result.Failure(FileError.NAME_NOT_VALID)
        } else if (fileProvider.getFileMimeType(source) == null || !(fileProvider.getFileMimeType(source)?:"").startsWith("video")) {
            Result.Failure(FileError.NOT_SUPPORTED)
        } else if (fileProvider.getFileSize(source)!! > ValidationRules.MAX_VIDEO_SIZE) {
            Result.Failure(FileError.LARGE)
        } else {
            Result.Success(Unit)
        }
    }
}