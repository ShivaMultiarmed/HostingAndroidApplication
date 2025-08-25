package mikhail.shell.video.hosting.domain.usecases.channels.validation

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.FileError.EMPTY
import mikhail.shell.video.hosting.domain.errors.FileError.LARGE
import mikhail.shell.video.hosting.domain.errors.FileError.NOT_SUPPORTED
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_IMAGE_SIZE
import javax.inject.Inject

class ValidateImage @Inject constructor(
    private val fileProvider: FileProvider
) {
    operator fun invoke(uri: String): Result<Unit, FileError> {
        val error = if (!fileProvider.exists(uri)) {
            EMPTY
        } else if (fileProvider.getFileMimeType(uri) == null) {
            NOT_SUPPORTED
        } else if (fileProvider.getFileSize(uri)!! > MAX_IMAGE_SIZE) {
            LARGE
        } else null
        return if (error != null) {
            Result.Failure(error)
        } else {
            Result.Success(Unit)
        }
    }
}