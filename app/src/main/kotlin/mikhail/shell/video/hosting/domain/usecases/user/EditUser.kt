package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import javax.inject.Inject

class EditUser @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User, avatar: String?, avatarAction: EditAction): Result<User, CompoundError<EditUserError>> {
        return userRepository.edit(user, avatar, avatarAction)
    }
}