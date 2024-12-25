package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Result

interface ChannelRepository {
    suspend fun fetchChannelInfo(channelId: Long, userId: Long): Result<ChannelWithUser, ChannelError>
}