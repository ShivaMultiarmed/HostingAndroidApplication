package mikhail.shell.video.hosting.presentation.user.screen

import androidx.compose.runtime.Immutable
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi
import mikhail.shell.video.hosting.presentation.user.models.UserUi

@Immutable
data class ProfileScreenState (
    val signedInUserId: Long,
    val user: UserUi? = null,
    val isStarting: Boolean = false,
    val error: Error? = null,
    val channelsState: OwnedChannelsState = OwnedChannelsState(),
    val isSigningOut: Boolean = false
)

@Immutable
data class OwnedChannelsState(
    val channels: List<ChannelUi>? = null,
    val hasMore: Boolean = true,
    val nextPartIndex: Int = 0,
    val error: Error? = null,
    val isStarting: Boolean = false,
    val isLoading: Boolean = false
)