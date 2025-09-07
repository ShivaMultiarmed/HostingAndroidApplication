package mikhail.shell.video.hosting.domain.utils

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateImage @Inject constructor(
    private val fileProvider: FileProvider
) {
    operator fun invoke(uri: String): Result<Unit, FileError> {
        val error = if (!fileProvider.exists(uri)) {
            FileError.NOT_FOUND
        } else if (fileProvider.getFileMimeType(uri) == null || !(fileProvider.getFileMimeType(uri)?:"").startsWith("image")) {
            FileError.NOT_SUPPORTED
        } else if (fileProvider.getFileSize(uri)!! == 0L) {
            FileError.EMPTY
        } else if (fileProvider.getFileSize(uri)!! > ValidationRules.MAX_IMAGE_SIZE) {
            FileError.LARGE
        } else null
        return if (error != null) {
            Result.Failure(error)
        } else {
            Result.Success(Unit)
        }
    }
}