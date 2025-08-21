package mikhail.shell.video.hosting.domain.usecases

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules

class ValidateChannelDescription {
    operator fun invoke(description: String): Result<Unit, TextError> {
        return if (description.length > ValidationRules.MAX_TEXT_LENGTH) {
            Result.Failure(TextError.LARGE)
        } else {
            Result.Success(Unit)
        }
    }
}