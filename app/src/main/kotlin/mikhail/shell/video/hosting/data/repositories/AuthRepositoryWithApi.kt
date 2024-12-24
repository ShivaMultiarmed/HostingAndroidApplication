package mikhail.shell.video.hosting.data.repositories

import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.domain.errors.AuthError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {
    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<AuthModel, AuthError> {
        return try {
            Result.Success(authApi.authWithEmailAndPassword(email, password))
        } catch (e: HttpException) {
            Result.Failure(AuthError.UNEXPECTED)
        } catch (e: Exception) {
            Result.Failure(AuthError.UNEXPECTED)
        }
    }
}