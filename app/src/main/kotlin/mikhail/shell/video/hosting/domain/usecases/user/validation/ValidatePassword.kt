package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_PASSWORD_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MIN_PASSWORD_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.PASSWORD_REGEX
import javax.inject.Inject

class ValidatePassword @Inject constructor() {
    operator fun invoke(password: String): Result<Unit, TextError> {
        return if (password.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            Result.Failure(TextError.SHORT)
        } else if (password.length > MAX_PASSWORD_LENGTH) {
            Result.Failure(TextError.LONG)
        } else if (!password.matches(PASSWORD_REGEX)) {
            Result.Failure(TextError.PATTERN)
        } else {
            Result.Success(Unit)
        }
    }
}