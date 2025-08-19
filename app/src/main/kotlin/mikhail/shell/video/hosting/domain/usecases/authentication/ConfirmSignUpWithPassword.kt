package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ConfirmSignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        token: String,
        password: String,
        user: User
    ): Result<AuthModel, Error> {
        val compoundError = CompoundError<SignUpError>()
        if (user.nick.length > ValidationRules.MAX_NAME_LENGTH) {
            compoundError.add(SignUpError.NICK_TOO_LARGE)
        }
        if (!password.matches(ValidationRules.PASSWORD_REGEX)) {
            compoundError.add(SignUpError.PASSWORD_NOT_VALID)
        }
        return if (compoundError.isNotEmpty()) {
            Result.Failure(compoundError)
        } else {
            authRepository.confirmSignUpWithPassword(
                token = token,
                password = password,
                user = user
            )
        }
    }
}