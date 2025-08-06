package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import mikhail.shell.video.hosting.domain.usecases.authentication.SignOut
import javax.inject.Inject

class RemoveUser @Inject constructor(
    private val signOut: SignOut,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit, Error> {
        signOut()
        return userRepository.remove()
    }
}
