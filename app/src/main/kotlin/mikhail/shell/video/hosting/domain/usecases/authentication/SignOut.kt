package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.SignOutError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.usecases.channels.Unsubscribe
import javax.inject.Inject

class SignOut @Inject constructor(
    private val authRepository: AuthRepository,
    private val unsubscribe: Unsubscribe
) {
    suspend operator fun invoke(userId: Long): Result<Unit, SignOutError> {
        return if (unsubscribe(userId) is Result.Success
            && authRepository.signOut(userId) is Result.Success
            ) {
            Result.Success(Unit)
        } else {
            Result.Failure(SignOutError.UNEXPECTED)
        }
    }
}