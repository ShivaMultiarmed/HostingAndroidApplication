package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.ExtendedChannelInfo
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class GetExtendedChannelInfo @Inject constructor(
    private val _channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channelId: Long, userId: Long): Result<ExtendedChannelInfo, ChannelError> {
        return _channelRepository.fetchExtendedChannelInfo(channelId, userId)
    }
}