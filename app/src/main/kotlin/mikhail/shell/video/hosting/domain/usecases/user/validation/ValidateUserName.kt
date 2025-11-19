package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.validation.CheckUserName
import javax.inject.Inject

class ValidateUserName @Inject constructor(
    private val validateEmail: ValidateEmail,
    private val checkUserName: CheckUserName
) {
    suspend operator fun invoke(
        purpose: UserNameCheckPurpose,
        userName: String
    ): Result<Unit, Error> {
        return if (userName.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else {
            val emailValidationResult = validateEmail(userName)
            if (emailValidationResult is Result.Failure) {
                Result.Failure(emailValidationResult.error)
            } else {
                val emailCheckResult = checkUserName(purpose, userName)
                if (emailCheckResult is Result.Failure) {
                    Result.Failure(emailCheckResult.error)
                } else {
                    Result.Success(Unit) as Result<Unit, TextError>
                }
            }
        }
    }
}