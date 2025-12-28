package mikhail.shell.video.hosting.domain.usecases.authentication.signup

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class VerifySignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        userName: String,
        code: String
    ): Result<String, Error> {
        return authRepository.verifySignUpWithPassword(userName, code)
    }
}