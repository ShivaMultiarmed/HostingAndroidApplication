package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.ExtendedChannelInfo
import mikhail.shell.video.hosting.domain.models.Result

interface ChannelRepository {
    suspend fun fetchExtendedChannelInfo(channelId: Long, userId: Long): Result<ExtendedChannelInfo, ChannelError>
}