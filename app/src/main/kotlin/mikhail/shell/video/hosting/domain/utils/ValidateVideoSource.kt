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
        if (source == null) {
            return Result.Failure(FileError.EMPTY)
        }
        val file = fileProvider.get(source)
        return when {
            file == null -> Result.Failure(FileError.NOT_FOUND)
            file.size == 0L -> Result.Failure(FileError.EMPTY)
            !file.name.matches(FILE_NAME_REGEX.toRegex())
                    || !file.mimeType.startsWith("video") -> Result.Failure(FileError.NOT_SUPPORTED)
            file.size > ValidationRules.MAX_VIDEO_SIZE -> Result.Failure(FileError.LARGE)
            else -> Result.Success(Unit)
        }
    }
}