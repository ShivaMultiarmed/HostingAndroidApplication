package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import javax.inject.Inject

class ValidatePasswordDuplicate @Inject constructor() {
    operator fun invoke(password: String, passwordDuplicate: String): Result<Unit, TextError> {
        return if (passwordDuplicate.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (password != passwordDuplicate) {
            Result.Failure(TextError.PATTERN)
        } else {
            Result.Success(Unit)
        }
    }
}