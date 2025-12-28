package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import javax.inject.Inject

class ValidateCode @Inject constructor() {
    operator fun invoke(code: String): Result<Unit, TextError> {
        return if (code.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (!code.matches(regex)) {
            Result.Failure(TextError.PATTERN)
        } else if (code.length > 4) {
            Result.Failure(TextError.LONG)
        } else {
            Result.Success(Unit)
        }
    }
    private companion object {
        val regex = Regex("[a-zA-Z0-9]{4}")
    }
}