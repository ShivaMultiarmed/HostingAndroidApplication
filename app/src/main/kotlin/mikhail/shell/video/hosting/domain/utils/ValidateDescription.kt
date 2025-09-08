package mikhail.shell.video.hosting.domain.utils

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import javax.inject.Inject

class ValidateDescription @Inject constructor() {
    operator fun invoke(description: String): Result<Unit, TextError> {
        return if (description.length > MAX_TEXT_LENGTH) {
            Result.Failure(TextError.LONG)
        } else {
            Result.Success(Unit)
        }
    }
}