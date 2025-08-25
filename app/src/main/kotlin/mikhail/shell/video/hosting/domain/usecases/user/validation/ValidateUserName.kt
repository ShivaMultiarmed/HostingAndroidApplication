package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import javax.inject.Inject

class ValidateUserName @Inject constructor(
    private val validateEmail: ValidateEmail,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userName: String): Result<Boolean, Error> {
        val localResult = validateEmail(userName)
        return if (localResult is Result.Failure) {
            Result.Failure(localResult.error)
        } else {
            Result.Success(true) as Result<Boolean, Error> // TODO
        }
    }
}