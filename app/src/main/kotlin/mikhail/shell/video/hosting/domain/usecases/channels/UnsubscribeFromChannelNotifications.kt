package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelSubscriptionError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class UnsubscribeFromChannelNotifications @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(userId: Long): Result<Unit, ChannelSubscriptionError> {
        return channelRepository.unsubscribeFromNotifications(userId)
    }
}