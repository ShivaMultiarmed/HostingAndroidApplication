package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.SignOutError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.usecases.channels.UnsubscribeFromChannelNotifications
import javax.inject.Inject

class SignOut @Inject constructor(
    private val authRepository: AuthRepository,
    private val unsubscribeFromChannelNotifications: UnsubscribeFromChannelNotifications
) {
    suspend operator fun invoke(userId: Long): Result<Unit, SignOutError> {
        val unsubscribeSuccess = unsubscribeFromChannelNotifications(userId) is Result.Success
        val signOutSuccess = authRepository.signOut(userId) is Result.Success
        return if (unsubscribeSuccess && signOutSuccess) {
            Result.Success(Unit)
        } else {
            Result.Failure(SignOutError.UNEXPECTED)
        }
    }
}