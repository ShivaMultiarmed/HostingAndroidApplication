package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Result

interface ChannelRepository {
    suspend fun fetchChannelForUser(
        channelId: Long,
        userId: Long
    ): Result<ChannelWithUser, ChannelError>
    suspend fun createChannel(
        channel: Channel
    ): Result<Channel, ChannelError>
    suspend fun fetchChannelsByOwner(
        userId: Long
    ): Result<List<Channel>, ChannelError>
}