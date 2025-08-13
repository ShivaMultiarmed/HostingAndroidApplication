package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class Subscribe @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(
        channel: ChannelWithUser,
        subscription: Subscription
    ): Result<ChannelWithUser, Error> {
        val result = channelRepository.subscribe(channel.channelId!!)

        return if (result is Result.Success<Unit>) {
            val newSubscription = when (channel.subscription) {
                Subscription.SUBSCRIBED -> Subscription.NOT_SUBSCRIBED
                Subscription.NOT_SUBSCRIBED -> Subscription.SUBSCRIBED
            }
            val newSubscribersNumber = when (newSubscription) {
                Subscription.SUBSCRIBED -> channel.subscribers + 1
                Subscription.NOT_SUBSCRIBED -> channel.subscribers - 1
            }
            val newChannel = channel.copy(
                subscription = newSubscription,
                subscribers = newSubscribersNumber
            )
            Result.Success(newChannel)
        } else {
            result as Result.Failure<Error>
        }
    }
}