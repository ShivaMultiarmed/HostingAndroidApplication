package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class SignInWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthModel, Error> {
        return authRepository.signInWithPassword(
            email = email,
            password = password
        )
    }
}