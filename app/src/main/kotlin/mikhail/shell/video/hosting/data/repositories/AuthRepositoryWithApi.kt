package mikhail.shell.video.hosting.data.repositories

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.dto.SignUpDto
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.isNetworkAvailable
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
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authApi: AuthApi,
    private val fcm: FirebaseMessaging,
    private val gson: Gson
) : AuthRepository {
    override suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<AuthModel, CompoundError<SignInError>> {
        return try {
            Result.Success(
                authApi.signInWithPassword(email, password)
            )
        } catch (e: HttpException) {
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<SignInError>>() {}.type
            val compoundError = gson.fromJson(json, type)?: CompoundError(SignInError.UNEXPECTED)
            Result.Failure(compoundError)
        } catch (e: Exception) {
            Result.Failure(CompoundError(SignInError.UNEXPECTED))
        }
    }

    override suspend fun signUpWithPassword(
        userName: String,
        password: String,
        user: User
    ): Result<AuthModel, Error> {
        return if (!appContext.isNetworkAvailable()) {
            try {
                val signUpDto = SignUpDto(
                    userName,
                    password,
                    user.toDto()
                )
                Result.Success(authApi.signUpWithPassword(signUpDto))
            } catch (e: HttpException) {
                val error: Error = when (e.code()) {
                    400 -> {
                        val json = e.response()?.errorBody()?.string()
                        val type = object : TypeToken<CompoundError<SignUpError>>() {}.type
                        gson.fromJson(json, type) ?: CompoundError(SignUpError.UNEXPECTED)
                    }

                    401, 403 -> NetworkError.FORBIDDEN
                    404 -> NetworkError.NOT_FOUND
                    in 500..599 -> NetworkError.SERVER_ERROR
                    else -> NetworkError.UNEXPECTED
                }
                Result.Failure(error)
            } catch (_: SocketTimeoutException) {
                Result.Failure(NetworkError.TIMEOUT_EXCEEDED)
            } catch (_: IOException) {
                Result.Failure(NetworkError.SERVER_NOT_AVAILABLE)
            } catch (_: Exception) {
                Result.Failure(CompoundError(SignUpError.UNEXPECTED))
            }
        } else Result.Failure(NetworkError.CONNECTION_ERROR)
    }
    override suspend fun signOut(userId: Long): Result<Unit, SignOutError> {
        return try {
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Failure(SignOutError.UNEXPECTED)
        }
    }
}