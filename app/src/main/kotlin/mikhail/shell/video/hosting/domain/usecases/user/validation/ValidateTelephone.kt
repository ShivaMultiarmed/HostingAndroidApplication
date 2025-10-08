package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEL_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MIN_TEL_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.TEL_REGEX
import javax.inject.Inject

class ValidateTelephone @Inject constructor() {
    operator fun invoke(telephone: String): Result<Unit, TextError> {
        return if (telephone.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (telephone.length < MIN_TEL_LENGTH) {
            Result.Failure(TextError.SHORT)
        } else if (telephone.length > MAX_TEL_LENGTH) {
            Result.Failure(TextError.LONG)
        } else if (!telephone.matches(TEL_REGEX)) {
            Result.Failure(TextError.PATTERN)
        } else {
            Result.Success(Unit)
        }
    }
}