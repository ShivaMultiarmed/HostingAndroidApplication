package mikhail.shell.video.hosting.data.repositories

import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.dto.SignUpDto
import mikhail.shell.video.hosting.data.dto.UserDto
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.AuthError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {
    override suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<AuthModel, AuthError> {
        return try {
            Result.Success(authApi.signInWithPassword(email, password))
        } catch (e: HttpException) {
            Result.Failure(AuthError.UNEXPECTED)
        } catch (e: Exception) {
            Result.Failure(AuthError.UNEXPECTED)
        }
    }

    override suspend fun signUpWithPassword(
        userName: String,
        password: String,
        user: User
    ): Result<AuthModel, AuthError> {
        return try {
            val signUpDto = SignUpDto(
                userName,
                password,
                user.toDto()
            )
            Result.Success(authApi.signUpWithPassword(signUpDto))
        } catch (e: HttpException) {
            val error = when (e.code()) {
                else -> AuthError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(AuthError.UNEXPECTED)
        }
    }

}