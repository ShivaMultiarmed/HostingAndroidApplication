package mikhail.shell.video.hosting.data.repositories

import android.webkit.MimeTypeMap
import com.google.gson.Gson
import mikhail.shell.video.hosting.data.api.UserApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserRepositoryWithApi @Inject constructor(
    private val userApi: UserApi,
    private val fileProvider: FileProvider,
    private val gson: Gson
) : UserRepository {
    private companion object {
        const val MAX_FILE_SIZE = 10 * 1024 * 1024
    }

    override suspend fun get(userId: Long): Result<User, Error> = request {
        userApi.get(userId).toDomain()
    }

    override suspend fun edit(
        user: User,
        avatar: String?,
        avatarAction: EditAction
    ): Result<User, Error> {
        return request {
            val avatarPart = avatar?.let { uri ->
                val bytes = fileProvider.getFileAsInputStream(uri).use { it?.readBytes() }
                val mimeType = fileProvider.getFileMimeType(uri)
                val mediaType = mimeType?.toMediaTypeOrNull()
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                val requestBody = bytes?.toRequestBody(contentType = mediaType)
                requestBody?.let {
                    MultipartBody.Part.createFormData(
                        name = "avatar",
                        filename = "avatar.$extension",
                        body = it
                    )
                }
            }
            userApi.edit(
                request = UserEditingRequest(
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
}

data class UserEditingRequest(
    val nick: String,
    val name: String?,
    val bio: String?,
    val tel: String?,
    val email: String?,
    val avatarAction: EditAction,
)