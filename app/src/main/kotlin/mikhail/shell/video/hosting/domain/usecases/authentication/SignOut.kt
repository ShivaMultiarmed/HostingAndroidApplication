package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.usecases.user.UnsubscribeFromNotifications
import javax.inject.Inject

class SignOut @Inject constructor(
    private val authRepository: AuthRepository,
    private val unsubscribeFromNotifications: UnsubscribeFromNotifications
) {
    suspend operator fun invoke(): Result<Unit, Error> {
        val unsubscribeSuccess = unsubscribeFromNotifications() is Result.Success
        val signOutSuccess = authRepository.signOut() is Result.Success
        return if (unsubscribeSuccess && signOutSuccess) {
            Result.Success(Unit)
        } else {
            Result.Failure(UnexpectedError)
        }
    }
}