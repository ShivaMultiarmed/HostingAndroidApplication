package mikhail.shell.video.hosting.presentation.subscriptions

import mikhail.shell.video.hosting.domain.errors.Error

sealed class SubscriptionsScreenEvent {
    data class Failure(val error: Error): SubscriptionsScreenEvent()
    data class ChannelChosen(val channelId: Long): SubscriptionsScreenEvent()
}