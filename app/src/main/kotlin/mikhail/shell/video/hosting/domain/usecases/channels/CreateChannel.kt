package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class CreateChannel @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channel: Channel): Result<Channel, ChannelError> {
        return channelRepository.createChannel(channel)
    }
}