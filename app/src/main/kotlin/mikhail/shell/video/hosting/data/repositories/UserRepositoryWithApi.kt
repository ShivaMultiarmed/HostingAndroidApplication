package mikhail.shell.video.hosting.data.repositories

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import mikhail.shell.video.hosting.BuildConfig.API_BASE_URL
import mikhail.shell.video.hosting.data.api.UserApi
import mikhail.shell.video.hosting.data.dto.EditingActionDto
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.invalidateCache
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.data.utils.uriToPart
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.user.UserEditingError
import mikhail.shell.video.hosting.domain.models.EditingAction
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.NickCheckPurpose
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.models.UserEditingModel
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import javax.inject.Inject

class UserRepositoryWithApi @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val userApi: UserApi,
    private val fileProvider: FileProvider,
    private val gson: Gson,
    private val fcm: FirebaseMessaging
) : UserRepository {

    override suspend fun get(userId: Long): Result<User, Error> = request {
        userApi.get(userId).toDomain()
    }

    override suspend fun edit(user: UserEditingModel): Result<User, Error> {
        return request (
            httpExceptionHandler(400) {
                val json = it.response()?.body() as String
                val response = gson.fromJson(json, UserEditingErrorResponse::class.java)
                UserEditingError(
                    nickError = response.nickError,
                    nameError = response.nameError,
                    bioError = response.bioError,
                    telError = response.telError,
                    emailError = response.emailError,
                    avatarError = response.avatarError
                )
            }
        ) {
            val avatarPart = when (user.avatar) {
                is EditingAction.Edit -> fileProvider.uriToPart(uri = user.avatar.value, partName = "avatar")
                else -> null
            }
            val editedUser = userApi.edit(
                user = UserEditingRequest(
                    nick = user.nick,
                    name = user.name,
                    bio = user.bio,
                    tel = user.tel,
                    email = user.email,
                    avatarAction = when (user.avatar) {
                        is EditingAction.Edit -> EditingActionDto.EDIT
                        EditingAction.Keep -> EditingActionDto.KEEP
                        EditingAction.Remove -> EditingActionDto.REMOVE
                    }
                ),
                avatar = avatarPart
            ).toDomain()
            ImageSize.entries.forEach { size ->
                appContext.invalidateCache(constructAvatarUrl(editedUser.userId, size))
            }
            return@request editedUser
        }
    }

    override suspend fun remove(): Result<Unit, Error> = request {
        userApi.remove()
    }

    override suspend fun existsByNick(purpose: NickCheckPurpose, nick: String): Result<Unit, Error> = request (
        httpExceptionHandler(409) {
            TextError.EXISTS
        }
    ) {
        userApi.existsByNick(
            purpose = purpose,
            nick = nick
        )
    }

    override fun constructAvatarUrl(
        userId: Long,
        size: ImageSize
    ): String {
        return "$API_BASE_URL/users/$userId/avatar?size=${size.name.lowercase()}"
    }

    override suspend fun subscribeToNotifications(): Result<Unit, Error> = request {
        userApi.subscribeToNotifications(fcm.token.await())
    }

    override suspend fun unsubscribeFromNotifications(): Result<Unit, Error> = request {
        userApi.unsubscribeFromNotifications(fcm.token.await())
    }
}

data class UserCreationErrorResponse(
    val nickError: TextError?,
    val passwordError: TextError?
)

data class UserEditingRequest(
    val nick: String,
    val name: String?,
    val bio: String?,
    val tel: String?,
    val email: String?,
    val avatarAction: EditingActionDto
)

data class UserEditingErrorResponse(
    val nickError: TextError?,
    val nameError: TextError?,
    val bioError: TextError?,
    val telError: TextError?,
    val emailError: TextError?,
    val avatarError: FileError?
)
