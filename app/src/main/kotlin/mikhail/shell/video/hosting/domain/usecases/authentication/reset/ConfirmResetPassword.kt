package mikhail.shell.video.hosting.domain.usecases.authentication.reset

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class ConfirmResetPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        token: String,
        password: String
    ): Result<AuthModel, Error> {
        return authRepository.confirmResetPassword(
            token = token,
            password = password
        )
    }
}