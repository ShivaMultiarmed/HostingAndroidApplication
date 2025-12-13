package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.errors.Error

sealed class VideoScreenEvent {
    data class Failure(val error: Error): VideoScreenEvent()
    data object EditRequested : VideoScreenEvent()
    data object ChannelRequested : VideoScreenEvent()
    data class ProfileRequested(val userId: Long) : VideoScreenEvent()
    data object Removed : VideoScreenEvent()
    data object DownloadRequested : VideoScreenEvent()
    data object SharingRequested : VideoScreenEvent()
}