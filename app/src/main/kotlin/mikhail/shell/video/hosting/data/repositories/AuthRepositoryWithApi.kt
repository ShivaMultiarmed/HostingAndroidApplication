package mikhail.shell.video.hosting.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.dto.SignUpDto
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.NetworkError
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.errors.SignOutError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authApi: AuthApi,
    private val gson: Gson
) : AuthRepository {
    override suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<AuthModel, Error> {
        return request(
            httpExceptionHandler(400) { e ->
                val json = e.response()?.errorBody()?.string()
                val type = object : TypeToken<CompoundError<SignInError>>() {}.type
                gson.fromJson(json, type) ?: NetworkError.UNEXPECTED
            }
        ) {
            authApi.signInWithPassword(email, password)
        }
    }

    override suspend fun signUpWithPassword(
        userName: String,
        password: String,
        user: User
    ): Result<AuthModel, Error> {
        return request (
            httpExceptionHandler(400) { e ->
                val json = e.response()?.errorBody()?.string()
                val type = object : TypeToken<CompoundError<SignUpError>>() {}.type
                gson.fromJson(json, type) ?: SignUpError.UNEXPECTED
            }
        ) {
            val signUpDto = SignUpDto(
                userName,
                password,
                user.toDto()
            )
            authApi.signUpWithPassword(signUpDto)
        }
    }

    override suspend fun signOut(userId: Long): Result<Unit, SignOutError> {
        return try {
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Failure(SignOutError.UNEXPECTED)
        }
    }
}