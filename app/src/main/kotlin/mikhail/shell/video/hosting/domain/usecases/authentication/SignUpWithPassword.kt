package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.errors.SignUpError.PASSWORD_NOT_VALID
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class SignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        userName: String,
        password: String,
        user: User
    ): Result<AuthModel, CompoundError<SignUpError>> {
        val compoundError = CompoundError<SignUpError>()
        if (userName.length > ValidationRules.MAX_USERNAME_LENGTH) {
            compoundError.add(SignUpError.USERNAME_TOO_LARGE)
        }
        val passwordLengthRange = ValidationRules.MIN_PASSWORD_LENGTH .. ValidationRules.MAX_PASSWORD_LENGTH
        if (password.length !in passwordLengthRange) {
            compoundError.add(PASSWORD_NOT_VALID)
        }
        if (user.nick.length > ValidationRules.MAX_NAME_LENGTH) {
            compoundError.add(SignUpError.NICK_TOO_LARGE)
        }
        return if (compoundError.isNotNull()) {
            Result.Failure(compoundError)
        } else {
            authRepository.signUpWithPassword(
                userName = userName,
                password = password,
                user = user
            )
        }
    }
}