package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateNick @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(nick: String): Result<Boolean, Error> {
        return if (nick.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (nick.length > ValidationRules.MAX_USERNAME_LENGTH) {
            Result.Failure(TextError.LONG)
        } else {
            Result.Success(true) // TODO
        }
    }
}