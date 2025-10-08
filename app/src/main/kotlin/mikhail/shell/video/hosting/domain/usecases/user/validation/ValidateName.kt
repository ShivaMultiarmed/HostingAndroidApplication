package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_NAME_LENGTH
import javax.inject.Inject

class ValidateName @Inject constructor() {
    operator fun invoke(name: String): Result<Unit, TextError> {
        return if (name.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (name.length > MAX_NAME_LENGTH) {
            Result.Failure(TextError.LONG)
        } else {
            Result.Success(Unit)
        }
    }
}