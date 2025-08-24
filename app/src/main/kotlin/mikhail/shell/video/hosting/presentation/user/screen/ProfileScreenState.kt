package mikhail.shell.video.hosting.presentation.user.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi
import mikhail.shell.video.hosting.presentation.user.models.UserUi

sealed class ProfileScreenState {
    data object Loading: ProfileScreenState()
    data class Success(
        val user: UserUi,
        val channelState: OwnedChannelsState
    ): ProfileScreenState()
    data class Failure(val error: Error): ProfileScreenState()
}

data class OwnedChannelsState(
    val channels: List<ChannelUi>? = null,
    val error: Error? = null,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false
)
