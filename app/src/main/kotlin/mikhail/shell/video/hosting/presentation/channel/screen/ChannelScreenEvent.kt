package mikhail.shell.video.hosting.presentation.channel.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenEvent as ScreenEvent

sealed class ChannelScreenEvent {
    data class Failure(val error: Error) : ScreenEvent()
    data class VideoChosen(val videoId: Long) : ScreenEvent()
    data class EditingRequest(val channelId: Long) : ScreenEvent()
    data class Removed(val channelId: Long) : ScreenEvent()
}