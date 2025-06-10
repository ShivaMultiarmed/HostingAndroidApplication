package mikhail.shell.video.hosting.data.repositories

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.UserApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.GetUserError
import mikhail.shell.video.hosting.domain.errors.RemoveUserError
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
    private val userApi: UserApi,
    private val fileProvider: FileProvider,
    private val gson: Gson
): UserRepository {
    private val MAX_FILE_SIZE = 10 * 1024 * 1024

    override suspend fun get(userId: Long): Result<User, GetUserError> {
        return try {
            val userDto = userApi.get(userId)
            val user = userDto.toDomain()
            Result.Success(user)
        } catch (e: HttpException) {
            val error = when (e.code()) {
                404 -> GetUserError.NOT_FOUND
                else -> GetUserError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(GetUserError.UNEXPECTED)
        }
    }

    override suspend fun edit(
        user: User,
        avatar: String?,
        avatarAction: EditAction
    ): Result<User, CompoundError<EditUserError>> {
        val compoundError = CompoundError<EditUserError>()
        return try {
            val userDto = user.toDto()
            val avatarPart = avatar?.let {
                val uri = Uri.parse(it)
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
                requestBody?.let { MultipartBody.Part.createFormData("avatar", "avatar.$extension", it) }
            }
            if (compoundError.isNotNull()) {
                return Result.Failure(compoundError)
            } else {
                val editedUserDto = userApi.edit(userDto, avatarAction, avatarPart)
                val editedUser = editedUserDto.toDomain()
                Result.Success(editedUser)
            }
        } catch (e: HttpException) {
            val error = when(e.code()) {
                400 -> {
                    val type = object : TypeToken<CompoundError<EditUserError>>() {}.type
                    val json = e.response()?.errorBody()?.string()
                    gson.fromJson(json, type)
                }
                401, 403 -> CompoundError(EditUserError.FORBIDDEN)
                404 -> CompoundError(EditUserError.USER_NOT_FOUND)
                else -> CompoundError(EditUserError.UNEXPECTED)
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Log.e("UserRepositoryWithApi", e.stackTraceToString())
            val error = CompoundError(EditUserError.UNEXPECTED)
            Result.Failure(error)
        }
    }

    override suspend fun remove(userId: Long): Result<Unit, RemoveUserError> {
        return try {
            userApi.remove(userId)
            Result.Success(Unit)
        } catch (e: HttpException) {
            val error = when(e.code()) {
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
