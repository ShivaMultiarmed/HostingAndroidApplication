package mikhail.shell.video.hosting.data.repositories

import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.ChannelInfo
import mikhail.shell.video.hosting.domain.models.ExtendedChannelInfo
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import retrofit2.HttpException
import javax.inject.Inject

class ChannelRepositoryWithApi @Inject constructor(
    private val _channelApi: ChannelApi
) : ChannelRepository {
    override suspend fun fetchExtendedChannelInfo(
        channelId: Long,
        userId: Long
    ): Result<ExtendedChannelInfo, ChannelError> {
        return try {
            Result.Success(_channelApi.fetchExtendedChannelInfo(channelId, userId))
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> Result.Failure(ChannelError.NOT_FOUND)
                else -> Result.Failure(ChannelError.UNEXPECTED)
            }
        } catch (e: Exception) {
            Result.Failure(ChannelError.UNEXPECTED)
        }
    }
}