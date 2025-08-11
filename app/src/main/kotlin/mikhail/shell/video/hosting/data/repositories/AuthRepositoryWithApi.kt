package mikhail.shell.video.hosting.data.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.dto.SignUpDto
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.authentication.SignInError
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
    private val authApi: AuthApi,
    private val gson: Gson
) : AuthRepository {
    override suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<AuthModel, Error> = request(
        httpExceptionHandler(400) { e ->
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<SignInError>>() {}.type
            gson.fromJson(json, type) ?: UnexpectedError
        }
    ) {
        authApi.signInWithPassword(email, password)
    }

    override suspend fun signUpWithPassword(
        userName: String,
        password: String,
        user: User
    ): Result<AuthModel, Error> = request (
        httpExceptionHandler(400) { e ->
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<SignUpError>>() {}.type
            gson.fromJson(json, type) ?: UnexpectedError
        }
    ) {
        val signUpDto = SignUpDto(
            userName,
            password,
            user.toDto()
        )
        authApi.signUpWithPassword(signUpDto)
    }

    override suspend fun signOut(): Result<Unit, Error> = request {
        authApi.signOut()
    }
}