package mikhail.shell.video.hosting.domain.usecases.authentication

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class SignInWithPassword @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        private val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    }
    suspend operator fun invoke(email: String, password: String): Result<AuthModel, CompoundError<SignInError>> {
        val compoundError = CompoundError<SignInError>()
        if (email.isEmpty())
            compoundError.add(SignInError.EMAIL_EMPTY)
        else if (!emailRegex.matches(email))
            compoundError.add(SignInError.EMAIL_INVALID)
        if (password.isEmpty())
            compoundError.add(SignInError.PASSWORD_EMPTY)
        return authRepository.signInWithPassword(email, password)
    }
}