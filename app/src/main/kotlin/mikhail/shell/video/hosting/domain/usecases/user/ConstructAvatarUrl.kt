package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import javax.inject.Inject

class ConstructAvatarUrl @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: Long, size: ImageSize): String {
        return userRepository.constructAvatarUrl(userId, size)
    }
}