package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User

interface AuthRepository {
    suspend fun signInWithPassword(
        email: String, password: String
    ): Result<AuthModel, CompoundError<SignInError>>

    suspend fun signUpWithPassword(
        userName: String, password: String, user: User
    ): Result<AuthModel, CompoundError<SignUpError>>
}