package mikhail.shell.video.hosting.domain.utils

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.ValidationRules.FILE_NAME_REGEX
import javax.inject.Inject

class ValidateImage @Inject constructor(
    private val fileProvider: FileProvider
) {
    operator fun invoke(uri: String): Result<Unit, FileError> {
        val file = fileProvider.get(uri)
        val error = when {
            file == null -> FileError.NOT_FOUND
            file.size == 0L -> FileError.EMPTY
            !file.name.matches(FILE_NAME_REGEX.toRegex())
                    || !file.mimeType.startsWith("image") -> FileError.NOT_SUPPORTED
            file.size > ValidationRules.MAX_IMAGE_SIZE -> FileError.LARGE
            else -> null
        }
        return if (error != null) {
            Result.Failure(error)
        } else {
            Result.Success(Unit)
        }
    }
}