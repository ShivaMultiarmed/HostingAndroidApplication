package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.ValidationRules.PASSWORD_REGEX
import javax.inject.Inject

class ValidatePassword @Inject constructor() {
    operator fun invoke(password: String): Result<Unit, TextError> {
        return if (password.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (password.length < ValidationRules.MIN_PASSWORD_LENGTH) {
            Result.Failure(TextError.SHORT)
        } else if (password.length > ValidationRules.MAX_PASSWORD_LENGTH) {
            Result.Failure(TextError.LONG)
        } else if (!password.matches(PASSWORD_REGEX)) {
            Result.Failure(TextError.PATTERN)
        } else {
            Result.Success(Unit)
        }
    }
}