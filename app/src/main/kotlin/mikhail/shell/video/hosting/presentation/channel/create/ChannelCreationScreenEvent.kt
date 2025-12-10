package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.errors.Error

sealed class ChannelCreationScreenEvent {
    data class Created(val channelId: Long): ChannelCreationScreenEvent()
    data class Failure(val error: Error): ChannelCreationScreenEvent()
    data object Cancelled: ChannelCreationScreenEvent()
}