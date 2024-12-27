package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.errors.SignUpError.*
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import javax.inject.Inject

class SignUpWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        userName: String,
        password: String,
        user: User
    ): Result<AuthModel, CompoundError<SignUpError>> {
        return authRepository.signUpWithPassword(
            userName,
            password,
            user
        )
    }
}