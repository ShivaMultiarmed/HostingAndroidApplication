package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose

interface AuthRepository {
    suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<AuthModel, Error>

    suspend fun checkUserName(purpose: UserNameCheckPurpose, userName: String): Result<Unit, Error>

    suspend fun requestSignUpWithPassword(userName: String): Result<Unit, Error>

    suspend fun verifySignUpWithPassword(userName: String, code: String): Result<String, Error>

    suspend fun confirmSignUpWithPassword(
        token: String,
        password: String,
        user: User
    ): Result<AuthModel, Error>

    suspend fun signOut(): Result<Unit, Error>

    suspend fun requestResetPassword(userName: String): Result<Unit, Error>

    suspend fun verifyResetPassword(userName: String, code: String): Result<String, Error>

    suspend fun confirmResetPassword(token: String, password: String): Result<AuthModel, Error>
}