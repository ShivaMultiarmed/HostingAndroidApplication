package mikhail.shell.video.hosting.domain.usecases.authentication.signup

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
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
        return if (code.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (!code.matches(regex)) {
            Result.Failure(TextError.PATTERN)
        } else if (code.length > 4) {
            Result.Failure(TextError.LONG)
        }else {
            authRepository.verifySignUpWithPassword(userName, code)
        }
    }

    private companion object {
        val regex = Regex("[a-zA-Z0-9]{4}")
    }
}