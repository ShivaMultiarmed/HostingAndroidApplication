package mikhail.shell.video.hosting.data.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.dto.SignUpDto
import mikhail.shell.video.hosting.data.dto.UserDto
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.AuthError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.errors.SignUpError.UNEXPECTED
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
    private val authApi: AuthApi,
    private val gson: Gson
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
    ): Result<AuthModel, CompoundError<SignUpError>> {
        return try {
            val signUpDto = SignUpDto(
                userName,
                password,
                user.toDto()
            )
            Result.Success(authApi.signUpWithPassword(signUpDto))
        } catch (e: HttpException) {
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<SignUpError>>() {}.type
            val compoundError = gson.fromJson(json, type)?: DEFAULT_ERROR
            Result.Failure(compoundError)
        } catch (e: Exception) {
            Result.Failure(DEFAULT_ERROR)
        }
    }
    companion object {
        private val DEFAULT_ERROR = CompoundError(mutableListOf(UNEXPECTED))
    }
}