package mikhail.shell.video.hosting.domain.usecases.authentication.signup

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class RequestSignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userName: String): Result<Unit, Error> {
        return authRepository.requestSignUpWithPassword(userName)
    }
}