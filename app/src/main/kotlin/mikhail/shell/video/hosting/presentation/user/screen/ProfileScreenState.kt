package mikhail.shell.video.hosting.presentation.user.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi
import mikhail.shell.video.hosting.presentation.user.models.UserUi

data class ProfileScreenState (
    val isLoading: Boolean = false,
    val user: UserUi? = null,
    val error: Error? = null,
    val channelState: OwnedChannelsState = OwnedChannelsState(),
    val signedOut: Boolean = false
)

data class OwnedChannelsState(
    val channels: List<ChannelUi>? = null,
    val error: Error? = null,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false
)
