package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.errors.Error

sealed class ChannelScreenEvent {
    data class Failure(val error: Error) : ChannelScreenEvent()
    data class VideoChosen(val videoId: Long) : ChannelScreenEvent()
    data class EditingRequest(val channelId: Long) : ChannelScreenEvent()
    data class Removed(val channelId: Long) : ChannelScreenEvent()
}