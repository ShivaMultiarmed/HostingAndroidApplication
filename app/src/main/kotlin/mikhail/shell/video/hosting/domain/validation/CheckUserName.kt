package mikhail.shell.video.hosting.domain.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class CheckUserName @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userName: String): Result<Boolean, Error> {
        return authRepository.existsByUserName(userName)
    }
}