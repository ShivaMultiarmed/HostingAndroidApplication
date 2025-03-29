package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.errors.GetUserError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import javax.inject.Inject

class GetUser @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long): Result<User, GetUserError> {
        return userRepository.get(userId)
    }
}