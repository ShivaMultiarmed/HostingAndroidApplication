package mikhail.shell.video.hosting.presentation.user.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi
import mikhail.shell.video.hosting.presentation.user.models.UserUi

data class ProfileScreenState (
    val isStarting: Boolean = false,
    val user: UserUi? = null,
    val error: Error? = null,
    val channelState: OwnedChannelsState = OwnedChannelsState(),
    val isSigningOut: Boolean = false
)

data class OwnedChannelsState(
    val channels: List<ChannelUi>? = null,
    val hasMore: Boolean = true,
    val nextPartIndex: Int = 0,
    val error: Error? = null,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false
)