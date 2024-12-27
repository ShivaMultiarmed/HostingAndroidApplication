package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.utils.isBlank
import mikhail.shell.video.hosting.presentation.channel.create.ChannelInputState
import javax.inject.Inject

class CreateChannel @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(
        channel: Channel
    ): Result<Channel, CompoundError<ChannelCreationError>> {
        return channelRepository.createChannel(channel)
    }
}
