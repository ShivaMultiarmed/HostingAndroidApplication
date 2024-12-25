package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.AuthError
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
        signUpInputState: SignUpInputState
    ): Result<AuthModel, AuthError> {
        if (signUpInputState.name == "") {
            return Result.Failure(AuthError.FIELDS_EMPTY)
        } else {
            val user = User(
                name = signUpInputState.name
            )
            return authRepository.signUpWithPassword(
                signUpInputState.userName,
                signUpInputState.password,
                user
            )
        }
    }
}