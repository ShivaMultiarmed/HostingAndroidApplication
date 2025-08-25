package mikhail.shell.video.hosting.domain.usecases.videos.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateSearchQuery @Inject constructor() {
    operator fun invoke(query: String): Result<Unit, TextError> {
        return if (query.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (query.length > ValidationRules.MAX_TEXT_LENGTH) {
            Result.Failure(TextError.LONG)
        } else {
            Result.Success(Unit)
        }
    }
}