package mikhail.shell.video.hosting.domain.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import javax.inject.Inject

class CheckUserName @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        purpose: UserNameCheckPurpose,
        userName: String
    ): Result<Unit, Error> {
        return authRepository.checkUserName(purpose, userName)
    }
}