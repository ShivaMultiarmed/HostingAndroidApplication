package mikhail.shell.video.hosting.data.repositories

import android.net.Uri
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.ChannelSubscriptionError
import mikhail.shell.video.hosting.domain.errors.ChannelSubscriptionError.RESUBSCRIBING_FAILED
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class ChannelRepositoryWithApi @Inject constructor(
    private val _channelApi: ChannelApi,
    private val gson: Gson,
    private val fcm: FirebaseMessaging,
    private val fileProvider: FileProvider
) : ChannelRepository {

    private val BUFFER_SIZE = 10 * 1024 * 1024

    override suspend fun fetchChannelForUser(
        channelId: Long,
        userId: Long
    ): Result<ChannelWithUser, ChannelLoadingError> {
        return try {
            Result.Success(_channelApi.fetchChannelDetails(channelId, userId).toDomain())
        } catch (e: HttpException) {
            val error = when (e.code()) {
                403 -> ChannelLoadingError.USER_NOT_SPECIFIED
                404 -> ChannelLoadingError.NOT_FOUND
                else -> ChannelLoadingError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(ChannelLoadingError.UNEXPECTED)
        }
    }

    override suspend fun createChannel(
        channel: Channel,
        avatar: File?,
        cover: File?
    ): Result<Channel, CompoundError<ChannelCreationError>> {
        return try {
            val avatarPart = avatar?.toPart("avatar")
            val coverPart = cover?.toPart("cover")
            val response = _channelApi.createChannel(
                channel.toDto(),
                avatarPart,
                coverPart
            )
            Result.Success(response.toDomain())
        } catch (e: HttpException) {
            val responseBody = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<ChannelCreationError>>() {}.type
            val error = gson.fromJson<CompoundError<ChannelCreationError>>(responseBody, type)
            Result.Failure(error)
        } catch (e: Exception) {
            val error = CompoundError(mutableListOf(ChannelCreationError.UNEXPECTED))
            Result.Failure(error)
        }
    }

    override suspend fun fetchChannelsByOwner(userId: Long): Result<List<Channel>, ChannelLoadingError> {
        return try {
            Result.Success(_channelApi.getChannelsByOwner(userId).map { it.toDomain() })
        } catch (e: HttpException) {
            val error = when (e.code()) {
                403 -> ChannelLoadingError.USER_NOT_SPECIFIED
                404 -> ChannelLoadingError.NOT_FOUND
                else -> ChannelLoadingError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(ChannelLoadingError.UNEXPECTED)
        }
    }

    override suspend fun fetchChannelsBySubscriber(userId: Long): Result<List<Channel>, ChannelLoadingError> {
        return try {
            Result.Success(_channelApi.getChannelsBySubscriber(userId).map { it.toDomain() })
        } catch (e: HttpException) {
            val error = when (e.code()) {
                403 -> ChannelLoadingError.USER_NOT_SPECIFIED
                404 -> ChannelLoadingError.NOT_FOUND
                else -> ChannelLoadingError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(ChannelLoadingError.UNEXPECTED)
        }
    }

    override suspend fun subscribe(
        channelId: Long,
        userId: Long,
        subscriptionState: SubscriptionState
    ): Result<ChannelWithUser, ChannelLoadingError> {
        return try {
            val token = fcm.token.await()
            Result.Success(
                _channelApi.subscribe(channelId, userId, token, subscriptionState).toDomain()
            )
        } catch (e: HttpException) {
            val error = when (e.code()) {
                403 -> ChannelLoadingError.USER_NOT_SPECIFIED
                404 -> ChannelLoadingError.NOT_FOUND
                else -> ChannelLoadingError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(ChannelLoadingError.UNEXPECTED)
        }
    }

    override suspend fun resubscribe(
        userId: Long
    ): Result<Void, ChannelSubscriptionError> {
        return try {
            val token = fcm.token.result
            Result.Success(_channelApi.resubscribe(userId, token))
        } catch (e: HttpException) {
            Result.Failure(RESUBSCRIBING_FAILED)
        } catch (e: Exception) {
            Result.Failure(RESUBSCRIBING_FAILED)
        }
    }

    override suspend fun editChannel(
        channel: Channel,
        editCoverAction: EditAction,
        cover: String?,
        editAvatarAction: EditAction,
        avatar: String?
    ): Result<Channel, CompoundError<EditChannelError>> {
        val localError = CompoundError<EditChannelError>()
        val coverPart = cover?.let {
            val isCoverTooLarge = (fileProvider.getFileSize(Uri.parse(it)) ?: 0) > BUFFER_SIZE
            if (editCoverAction == EditAction.UPDATE && !isCoverTooLarge) {
                fileProvider.uriToPart("cover", it)
            } else {
                if (isCoverTooLarge) {
                    localError.add(EditChannelError.COVER_TOO_LARGE)
                }
                null
            }
        }
        val avatarPart = avatar?.let {
            val isAvatarTooLarge = (fileProvider.getFileSize(Uri.parse(it)) ?: 0) > BUFFER_SIZE
            if (editAvatarAction == EditAction.UPDATE && !isAvatarTooLarge) {
                fileProvider.uriToPart("avatar", it)
            } else {
                if (isAvatarTooLarge) {
                    localError.add(EditChannelError.AVATAR_TOO_LARGE)
                }
                null
            }
        }
        if (localError.isNotNull()) {
            return Result.Failure(localError)
        } else {
            return try {
                val editedChannel = _channelApi.editChannel(
                    channel.toDto(),
                    coverPart,
                    avatarPart,
                    editCoverAction,
                    editAvatarAction
                ).toDomain()
                Result.Success(editedChannel)
            } catch (e: HttpException) {
                val error = CompoundError<EditChannelError>()
                error.add(EditChannelError.UNEXPECTED)
                Result.Failure(error)
            } catch (e: Exception) {
                val error = CompoundError<EditChannelError>()
                error.add(EditChannelError.UNEXPECTED)
                Result.Failure(error)
            }
        }
    }

    override suspend fun fetchChannel(channelId: Long): Result<Channel, ChannelLoadingError> {
        return try {
            val fetchedChannel = _channelApi.fetchChannel(channelId).toDomain()
            Result.Success(fetchedChannel)
        } catch (e: HttpException) {
            Result.Failure(ChannelLoadingError.UNEXPECTED)
        } catch (e: Exception) {
            Result.Failure(ChannelLoadingError.UNEXPECTED)
        }
    }
}