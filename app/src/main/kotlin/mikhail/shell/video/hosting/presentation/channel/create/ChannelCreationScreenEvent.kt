package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreenEvent as ScreenEvent

sealed class ChannelCreationScreenEvent {
    data class Created(val channelId: Long): ScreenEvent()
    data class Failure(val error: Error): ScreenEvent()
    data object Cancelled: ScreenEvent()
}