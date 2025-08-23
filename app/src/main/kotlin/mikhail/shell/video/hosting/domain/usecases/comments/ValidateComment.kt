package mikhail.shell.video.hosting.domain.usecases.comments

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules

class ValidateComment {
    operator fun invoke(text: String): Result<Unit, TextError> {
        return if (text.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (text.length > ValidationRules.MAX_TEXT_LENGTH) {
            Result.Failure(TextError.LARGE)
        } else {
            Result.Success(Unit)
        }
    }
}