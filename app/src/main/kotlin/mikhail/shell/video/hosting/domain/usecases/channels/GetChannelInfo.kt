package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class GetChannelInfo @Inject constructor(
    private val _channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channelId: Long, userId: Long): Result<Channel, ChannelError> {
        return _channelRepository.fetchChannelInfo(channelId, userId)
    }
}