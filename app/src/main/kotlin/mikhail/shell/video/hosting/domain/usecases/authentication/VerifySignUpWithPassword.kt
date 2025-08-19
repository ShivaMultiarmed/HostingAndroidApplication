package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class VerifySignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userName: String, code: String): Result<String, Error> {
        val compoundError = CompoundError<SignUpError>()
        if (code.length != ValidationRules.CODE_LENGTH) {
            compoundError.add(SignUpError.CODE_LENGTH_NOT_CORRECT)
        }
        return if (compoundError.isNotEmpty()) {
            Result.Failure(compoundError)
        } else {
            authRepository.verifySignUpWithPassword(userName, code)
        }
    }
}