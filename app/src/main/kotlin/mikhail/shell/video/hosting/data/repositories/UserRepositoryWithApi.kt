package mikhail.shell.video.hosting.data.repositories

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import mikhail.shell.video.hosting.data.api.UserApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.RemoveUserError
import mikhail.shell.video.hosting.domain.errors.ValidationException
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.UserRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject

class UserRepositoryWithApi @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userApi: UserApi,
    private val fileProvider: FileProvider,
    private val gson: Gson
) : UserRepository {
    private val MAX_FILE_SIZE = 10 * 1024 * 1024

    override suspend fun get(userId: Long): Result<User, Error> {
        return request {
            userApi.get(userId).toDomain()
        }
    }

    override suspend fun edit(
        user: User,
        avatar: String?,
        avatarAction: EditAction
    ): Result<User, Error> {
        return request (
            httpExceptionHandler(400) { e ->
                val type = object : TypeToken<CompoundError<EditUserError>>() {}.type
                val json = e.response()?.errorBody()?.string()
                gson.fromJson(json, type)
            }
        ) {
            val compoundError = CompoundError<EditUserError>()
            val userDto = user.toDto()
            val avatarPart = avatar?.let {
                val uri = it.toUri()
                if (fileProvider.getFileMimeType(uri)!!.substringBefore("/") != "image") {
                    compoundError.add(EditUserError.AVATAR_TYPE_NOT_VALID)
                    return@let null
                }
                if (fileProvider.getFileSize(uri)!! > MAX_FILE_SIZE) {
                    compoundError.add(EditUserError.AVATAR_TOO_LARGE)
                    return@let null
                }
                val bytes = fileProvider.getFileAsInputStream(uri).use { it?.readBytes() }
                val mimeType = fileProvider.getFileMimeType(uri)
                val mediaType = mimeType?.toMediaTypeOrNull()
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                val requestBody = bytes?.toRequestBody(contentType = mediaType)
                requestBody?.let {
                    MultipartBody.Part.createFormData(
                        "avatar",
                        "avatar.$extension",
                        it
                    )
                }
            }
            if (compoundError.isNotNull()) {
                throw ValidationException(compoundError)
            } else {
                userApi.edit(
                    user = userDto,
                    avatarAction = avatarAction,
                    avatar = avatarPart
                ).toDomain()
            }
        }
    }

    override suspend fun remove(userId: Long): Result<Unit, RemoveUserError> {
        return try {
            userApi.remove(userId)
            Result.Success(Unit)
        } catch (e: HttpException) {
            val error = when (e.code()) {
                401, 403 -> RemoveUserError.FORBIDDEN
                404 -> RemoveUserError.NOT_FOUND
                else -> RemoveUserError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(RemoveUserError.UNEXPECTED)
        }
    }

}
