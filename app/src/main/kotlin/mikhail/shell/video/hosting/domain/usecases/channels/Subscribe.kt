package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class Subscribe @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(
        channelId: Long,
        subscriptionState: SubscriptionState
    ): Result<Unit, Error> = channelRepository.subscribe(channelId) // TODO: move business logic here
}