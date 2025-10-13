package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import javax.inject.Inject

class GetVideoMetaData @Inject constructor(
    private val fileProvider: FileProvider
) {
    suspend operator fun invoke(uri: String): Result<File, FileError> {
        return fileProvider.getFile(uri) ?.let {
            Result.Success(it)
        }?: return Result.Failure(FileError.NOT_FOUND)
    }
}