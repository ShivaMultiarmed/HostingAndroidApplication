package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateComment @Inject constructor() {
    operator fun invoke(text: String): Result<Unit, TextError> {
        return if (text.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (text.length > ValidationRules.MAX_TEXT_LENGTH) {
            Result.Failure(TextError.LONG)
        } else {
            Result.Success(Unit)
        }
    }
}