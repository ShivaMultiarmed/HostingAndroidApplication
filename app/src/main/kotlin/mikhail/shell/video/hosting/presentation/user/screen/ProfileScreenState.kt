package mikhail.shell.video.hosting.presentation.user.screen

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.presentation.user.UserModel

data class ProfileScreenState(
    val user: UserModel? = null,
    val channels: List<Channel>? = null,
    val userError: Error? = null,
    val channelError: ChannelLoadingError? = null,
    val isLoading: Boolean = false
)
