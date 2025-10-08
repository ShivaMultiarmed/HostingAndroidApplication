package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_EMAIL_LENGTH
import javax.inject.Inject

class ValidateEmail @Inject constructor() {
    operator fun invoke(email: String): Result<Unit, TextError> {
        return if (email.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (email.length > MAX_EMAIL_LENGTH) {
            Result.Failure(TextError.LONG)
        } else if (!email.matches(regex)) {
            Result.Failure(TextError.PATTERN)
        } else {
            Result.Success(Unit)
        }
    }
    private companion object {
        val regex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    }
}