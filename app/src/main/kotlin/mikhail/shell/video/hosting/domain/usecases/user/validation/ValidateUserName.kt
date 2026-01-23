package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.UserNameCheckPurpose
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class ValidateUserName @Inject constructor(
    private val validateEmail: ValidateEmail,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        purpose: UserNameCheckPurpose,
        userName: String
    ): Result<Unit, Error> {
        val emailValidationResult = validateEmail(userName)
        return if (emailValidationResult is Result.Failure) {
            Result.Failure(emailValidationResult.error)
        } else {
            val emailCheckResult = authRepository.checkUserName(purpose, userName)
            if (emailCheckResult is Result.Failure) {
                Result.Failure(emailCheckResult.error)
            } else {
                Result.Success(Unit)
            }
        }
    }
}