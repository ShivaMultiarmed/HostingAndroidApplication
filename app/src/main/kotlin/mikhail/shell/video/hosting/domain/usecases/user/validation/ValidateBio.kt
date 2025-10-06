package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import javax.inject.Inject

class ValidateBio @Inject constructor() {
    operator fun invoke(bio: String): Result<Unit, TextError> {
        return if (bio.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (bio.length > MAX_TEXT_LENGTH) {
            Result.Failure(TextError.LONG)
        } else {
            Result.Success(Unit)
        }
    }
}