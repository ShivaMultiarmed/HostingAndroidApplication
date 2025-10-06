package mikhail.shell.video.hosting.data.repositories

import com.google.gson.Gson
import mikhail.shell.video.hosting.data.api.UserApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.data.utils.uriToPart
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.UserEditingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import javax.inject.Inject

class UserRepositoryWithApi @Inject constructor(
    private val userApi: UserApi,
    private val fileProvider: FileProvider,
    private val gson: Gson
) : UserRepository {

    override suspend fun get(userId: Long): Result<User, Error> = request {
        userApi.get(userId).toDomain()
    }

    override suspend fun edit(
        user: User,
        avatar: String?,
        avatarAction: EditAction
    ): Result<User, Error> {
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
            val avatarPart = avatar?.let {
                fileProvider.uriToPart(uri = it, partName = "avatar")
            }
            userApi.edit(
                user = UserEditingRequest(
                    nick = user.nick,
                    name = user.name,
                    bio = user.bio,
                    tel = user.tel,
                    email = user.email,
                    avatarAction = avatarAction
                ),
                avatar = avatarPart
            ).toDomain()
        }
    }

    override suspend fun remove(): Result<Unit, Error> = request {
        userApi.remove()
    }

    override suspend fun existsByNick(nick: String, userId: Long?): Result<Boolean, Error> = request {
        userApi.existsByNick(nick, userId)
    }
}

data class UserEditingErrorResponse(
    val nickError: TextError?,
    val nameError: TextError?,
    val bioError: TextError?,
    val telError: TextError?,
    val emailError: TextError?,
    val avatarError: FileError?
)

data class UserEditingRequest(
    val nick: String,
    val name: String?,
    val bio: String?,
    val tel: String?,
    val email: String?,
    val avatarAction: EditAction
)