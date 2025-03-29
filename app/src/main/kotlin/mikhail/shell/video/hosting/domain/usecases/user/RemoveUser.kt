package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.errors.RemoveUserError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import javax.inject.Inject

class RemoveUser @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long): Result<Unit, RemoveUserError> {
        return userRepository.remove(userId)
    }
}
