package mikhail.shell.video.hosting.domain.usecases.authentication.signup

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.models.UserCreationModel
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class ConfirmSignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        token: String,
        password: String,
        user: UserCreationModel
    ): Result<AuthModel, Error> {
        return authRepository.confirmSignUpWithPassword(
            token = token,
            password = password,
            user = user
        )
    }
}