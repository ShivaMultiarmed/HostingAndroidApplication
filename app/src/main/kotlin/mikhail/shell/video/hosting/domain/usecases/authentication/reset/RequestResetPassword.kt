package mikhail.shell.video.hosting.domain.usecases.authentication.reset

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class RequestResetPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userName: String): Result<Long, Error> {
        return authRepository.requestResetPassword(userName)
    }
}