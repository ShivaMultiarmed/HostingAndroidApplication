package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.authentication.ResetError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ConfirmResetPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        token: String,
        password: String
    ): Result<Unit, Error> {
        return if (!password.matches(ValidationRules.PASSWORD_REGEX)) {
            Result.Failure(ResetError.PASSWORD_NOT_VALID)
        } else {
            authRepository.confirmResetPassword(
                token = token,
                password = password
            )
        }
    }
}