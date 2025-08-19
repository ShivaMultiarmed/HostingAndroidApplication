package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class RequestSignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userName: String): Result<Unit, Error> {
        val compoundError = CompoundError<SignUpError>()
        if (userName.length > ValidationRules.MAX_USERNAME_LENGTH) {
            compoundError.add(SignUpError.USERNAME_TOO_LARGE)
        }
        return if (compoundError.isNotEmpty()) {
            Result.Failure(compoundError)
        } else {
            authRepository.requestSignUpWithPassword(userName)
        }
    }
}