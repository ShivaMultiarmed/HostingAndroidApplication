package mikhail.shell.video.hosting.presentation.subscriptions

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenEvent as ScreenEvent

sealed class SubscriptionsScreenEvent {
    data class Failure(val error: Error): ScreenEvent()
    data class ChannelChosen(val channelId: Long): ScreenEvent()
}