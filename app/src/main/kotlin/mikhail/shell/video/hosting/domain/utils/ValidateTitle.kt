package mikhail.shell.video.hosting.domain.utils

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateTitle @Inject constructor() {
    operator fun invoke(title: String): Result<Unit, TextError> {
        return if (title.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (title.length > ValidationRules.MAX_TITLE_LENGTH) {
            Result.Failure(TextError.LARGE)
        } else {
            Result.Success(Unit)
        }
    }
}