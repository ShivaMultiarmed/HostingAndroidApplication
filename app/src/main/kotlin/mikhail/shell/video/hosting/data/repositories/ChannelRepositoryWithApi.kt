package mikhail.shell.video.hosting.data.repositories

import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import retrofit2.HttpException
import javax.inject.Inject

class ChannelRepositoryWithApi @Inject constructor(
    private val _channelApi: ChannelApi
) : ChannelRepository {
    override suspend fun fetchChannelForUser(
        channelId: Long,
        userId: Long
    ): Result<ChannelWithUser, ChannelError> {
        return try {
            Result.Success(_channelApi.fetchChannelDetails(channelId, userId).toDomain())
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> Result.Failure(ChannelError.NOT_FOUND)
                else -> Result.Failure(ChannelError.UNEXPECTED)
            }
        } catch (e: Exception) {
            Result.Failure(ChannelError.UNEXPECTED)
        }
    }

    override suspend fun createChannel(channel: Channel): Result<Channel, ChannelError> {
        return try {
            Result.Success(_channelApi.createChannel(channel.toDto()).toDomain())
        } catch (e: HttpException) {
            val error = when (e.code()) {
                else -> ChannelError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(ChannelError.UNEXPECTED)
        }
    }

    override suspend fun fetchChannelsByOwner(userId: Long): Result<List<Channel>, ChannelError> {
        return try {
            Result.Success(_channelApi.getChannelsByOwner(userId).map { it.toDomain() })
        } catch (e: HttpException) {
            val error = when (e.code()) {
                else -> ChannelError.UNEXPECTED
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(ChannelError.UNEXPECTED)
        }
    }
}