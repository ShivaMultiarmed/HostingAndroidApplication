package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
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
        if (user.nick.length > ValidationRules.MAX_NAME_LENGTH) {
            compoundError.add(EditUserError.NICK_TOO_LARGE)
        }
        if ((user.name?.length?: 0) > ValidationRules.MAX_NAME_LENGTH) {
            compoundError.add(EditUserError.NAME_TOO_LARGE)
        }
        if ((user.bio?.length ?: 0) > ValidationRules.MAX_TEXT_LENGTH) {
            compoundError.add(EditUserError.BIO_TOO_LARGE)
        }
        if ((user.email?.length ?: 0) > ValidationRules.MAX_USERNAME_LENGTH) {
            compoundError.add(EditUserError.EMAIL_TOO_LARGE)
        }
        return if (compoundError.isNotNull()) {
            Result.Failure(compoundError)
        } else {
            userRepository.edit(user, avatar, avatarAction)
        }
    }
}