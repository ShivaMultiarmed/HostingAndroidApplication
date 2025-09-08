package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import javax.inject.Inject

class ValidateUserName @Inject constructor(
    private val validateEmail: ValidateEmail
) {
    operator fun invoke(userName: String): Result<Unit, TextError> {
        return if (userName.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else {
            val emailValidationResult = validateEmail(userName)
            if (emailValidationResult is Result.Failure) {
                Result.Failure(emailValidationResult.error)
            } else {
                Result.Success(Unit) as Result<Unit, TextError>
            }
        }
    }
}