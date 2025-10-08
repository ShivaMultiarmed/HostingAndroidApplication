package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.NickCheckPurpose
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateNick @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(purpose: NickCheckPurpose, nick: String): Result<Unit, Error> {
        return if (nick.isBlank()) {
            Result.Failure(TextError.EMPTY)
        } else if (nick.length > ValidationRules.MAX_USERNAME_LENGTH) {
            Result.Failure(TextError.LONG)
        } else {
            userRepository.existsByNick(purpose, nick)
        }
    }
}