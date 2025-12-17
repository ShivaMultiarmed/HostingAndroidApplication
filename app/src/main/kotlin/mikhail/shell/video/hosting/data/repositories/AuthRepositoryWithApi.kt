package mikhail.shell.video.hosting.data.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.dto.SignInErrorResponse
import mikhail.shell.video.hosting.data.dto.UserCreationRequest
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.authentication.SignInError
import mikhail.shell.video.hosting.domain.errors.user.UserCreationError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.UserCreationModel
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
    private val authApi: AuthApi,
    private val gson: Gson
) : AuthRepository {
    override suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<AuthModel, Error> = request(
        httpExceptionHandler(400) {
            val json = it.response()?.errorBody()?.string()?: return@httpExceptionHandler UnexpectedError
            val response = gson.fromJson(json, SignInErrorResponse::class.java)
            SignInError(
                userNameError = response.userNameError,
                passwordError = response.passwordError
            )
        }
    ) {
        authApi.signInWithPassword(email, password)
    }

    override suspend fun checkUserName(
        purpose: UserNameCheckPurpose,
        userName: String
    ): Result<Unit, Error> = request (
        httpExceptionHandler(409) {
            TextError.EXISTS
        },
        httpExceptionHandler(404) {
            TextError.NOT_EXISTS
        }
    ) {
        authApi.checkUserName(purpose, userName)
    }

    override suspend fun requestSignUpWithPassword(userName: String): Result<Unit, Error> = request (
        httpExceptionHandler(400) {
            val errors = it.response()?.getErrors<TextError>()
            errors?.get("user_name_error")?: UnexpectedError
        }
    ) {
        authApi.requestSignUpWithPassword(userName)
    }

    override suspend fun verifySignUpWithPassword(
        userName: String,
        code: String
    ): Result<String, Error> = request(
        httpExceptionHandler(400) {
            val errors = it.response()?.getErrors<TextError>()
            errors?.get("code_error")?: UnexpectedError
        }
    ) {
        authApi.verifySignUpWithPassword(userName, code)
    }

    override suspend fun confirmSignUpWithPassword(
        user: UserCreationModel
    ): Result<AuthModel, Error> = request (
        httpExceptionHandler(400) { e ->
            val json = e.response()?.errorBody()?.string()
            val response = gson.fromJson(json, UserCreationErrorResponse::class.java)
            UserCreationError(
                nickError = response.nickError,
                passwordError = response.passwordError
            )
        }
    ) {
        val userRequest = UserCreationRequest(
            password = user.password,
            nick = user.nick
        )
        authApi.confirmSignUpWithPassword(
            token = "Bearer ${user.token}",
            user = userRequest
        )
    }

    override suspend fun signOut(): Result<Unit, Error> = request {
        authApi.signOut()
    }

    override suspend fun requestResetPassword(userName: String): Result<Long, Error> = request(
        httpExceptionHandler(400) {
            val errors = it.response()?.getErrors<TextError>()
            errors?.get("user_name_error")?: UnexpectedError
        }
    ) {
        authApi.requestResetPassword(userName)
    }

    override suspend fun verifyResetPassword(
        userId: Long,
        code: String
    ): Result<String, Error> = request (
        httpExceptionHandler(400) {
            val errors = it.response()?.getErrors<TextError>()
            errors?.get("code_error")?: UnexpectedError
        }
    ) {
        authApi.verifyResetPassword(
            userId = userId,
            code = code
        )
    }

    override suspend fun confirmResetPassword(
        token: String,
        password: String
    ): Result<AuthModel, Error> = request (
        httpExceptionHandler(400) {
            val errors = it.response()?.getErrors<TextError>()
            errors?.get("password_error")?: UnexpectedError
        }
    ) {
        authApi.confirmResetPassword(
            token = token,
            password = password
        )
    }

    private inline fun <reified T: Enum<*>> Response<*>.getErrors(): Map<String, T>? {
        if (isSuccessful) return null
        val json = errorBody()!!.string()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson<Map<String, String>>(json, type).mapErrors<T>()
    }

    private inline fun <reified T: Enum<*>> Map<String, String>.mapErrors(): Map<String, T>? {
        return map {
            it.key to java.lang.Enum.valueOf(T::class.java as Class<out Enum<*>>, it.value.uppercase())
        }.toMap() as? Map<String, T>
    }
}