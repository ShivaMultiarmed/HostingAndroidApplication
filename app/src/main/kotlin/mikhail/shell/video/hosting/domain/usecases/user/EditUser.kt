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
    suspend operator fun invoke(
        user: User,
        avatar: String?,
        avatarAction: EditAction
    ): Result<User, CompoundError<EditUserError>> {
        val compoundError = CompoundError<EditUserError>()
        if (user.nick.length > 20) {
            compoundError.add(EditUserError.NICK_TOO_LARGE)
        }
        if ((user.name?.length?: 0) > 20) {
            compoundError.add(EditUserError.NAME_TOO_LARGE)
        }
        if ((user.bio?.length ?: 0) > 255) {
            compoundError.add(EditUserError.BIO_TOO_LARGE)
        }
        return if (compoundError.isNotNull()) {
            Result.Failure(compoundError)
        } else {
            userRepository.edit(user, avatar, avatarAction)
        }
    }
}