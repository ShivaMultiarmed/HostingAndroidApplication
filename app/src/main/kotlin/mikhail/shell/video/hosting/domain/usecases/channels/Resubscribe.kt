package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.errors.ChannelSubscriptionError
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class Resubscribe @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(userId: Long): Result<Void, ChannelSubscriptionError> {
        return channelRepository.resubscribe(userId)
    }
}