package mikhail.shell.video.hosting.presentation.user.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi
import mikhail.shell.video.hosting.presentation.user.models.UserUi

data class ProfileScreenState(
    val user: UserUi? = null,
    val channels: List<ChannelUi>? = null,
    val userError: Error? = null,
    val channelError: Error? = null,
    val isLoading: Boolean = false,
    val isSignedOut: Boolean? = null
)
