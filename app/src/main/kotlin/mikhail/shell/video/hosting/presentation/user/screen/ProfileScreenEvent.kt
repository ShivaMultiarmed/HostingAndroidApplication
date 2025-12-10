package mikhail.shell.video.hosting.presentation.user.screen

import mikhail.shell.video.hosting.domain.errors.Error

sealed class ProfileScreenEvent {
    data class Failure(val error: Error): ProfileScreenEvent()
    data object SignedOut: ProfileScreenEvent()
    data class ChannelChosen(val channelId: Long) : ProfileScreenEvent()
    data object SettingsRequested : ProfileScreenEvent()
    data object VideoUploadingRequested : ProfileScreenEvent()
    data object ChannelCreationRequested : ProfileScreenEvent()
    data object InvitationRequested : ProfileScreenEvent()
}