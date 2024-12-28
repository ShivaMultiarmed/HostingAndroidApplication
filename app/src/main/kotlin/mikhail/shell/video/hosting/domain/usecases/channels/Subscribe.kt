package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class Subscribe @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channelId: Long, userId: Long, subscriptionState: SubscriptionState): Result<ChannelWithUser, ChannelLoadingError> {
        return channelRepository.subscribe(channelId, userId, subscriptionState)
    }
}