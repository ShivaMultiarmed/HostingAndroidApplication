package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.AuthError
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
    private val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    suspend operator fun invoke(
        signUpInputState: SignUpInputState
    ): Result<AuthModel, CompoundError<SignUpError>> {
        val error = CompoundError<SignUpError>()
        if (signUpInputState.userName == "")
            error.add(EMAIL_EMPTY)
        else if (!emailRegex.matches(signUpInputState.userName))
            error.add(EMAIL_INVALID)
        if (signUpInputState.password == "")
            error.add(PASSWORD_EMPTY)
        if (signUpInputState.name == "")
            error.add(NAME_EMPTY)
        if (error.isNotNull())
            return Result.Failure(error)
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