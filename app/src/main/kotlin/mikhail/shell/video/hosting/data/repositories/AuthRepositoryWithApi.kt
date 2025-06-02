package mikhail.shell.video.hosting.data.repositories

import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.dto.SignUpDto
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.errors.SignOutError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryWithApi @Inject constructor(
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
    ): Result<AuthModel, CompoundError<SignUpError>> {
        return try {
            val signUpDto = SignUpDto(
                userName,
                password,
                user.toDto()
            )
            Result.Success(
                authApi.signUpWithPassword(signUpDto)
            )
        } catch (e: HttpException) {
            val json = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<SignUpError>>() {}.type
            val compoundError = gson.fromJson(json, type)?: CompoundError(SignUpError.UNEXPECTED)
            Result.Failure(compoundError)
        } catch (_: Exception) {
            Result.Failure(CompoundError(SignUpError.UNEXPECTED))
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