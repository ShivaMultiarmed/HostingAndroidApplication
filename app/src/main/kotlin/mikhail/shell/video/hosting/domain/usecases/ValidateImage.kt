package mikhail.shell.video.hosting.domain.usecases

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateImage @Inject constructor(
    private val fileProvider: FileProvider
) {
    suspend operator fun invoke(uri: String): Result<Unit, FileError> {
        val error = if (!fileProvider.exists(uri)) {
            FileError.EMPTY
        } else if (fileProvider.getFileMimeType(uri) == null) {
            FileError.NOT_SUPPORTED
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