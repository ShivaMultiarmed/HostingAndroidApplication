package mikhail.shell.video.hosting.data.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import retrofit2.HttpException
import javax.inject.Inject

class ChannelRepositoryWithApi @Inject constructor(
    private val _channelApi: ChannelApi,
    private val gson: Gson
) : ChannelRepository {
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

    override suspend fun createChannel(channel: Channel, avatar: File?, cover: File?): Result<Channel, CompoundError<ChannelCreationError>> {
        return try {
            val avatarPart = avatar?.fileToPart("avatar")
            val coverPart = cover?.fileToPart("cover")
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
            Result.Success(_channelApi.subscribe(channelId, userId, subscriptionState).toDomain())
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
}