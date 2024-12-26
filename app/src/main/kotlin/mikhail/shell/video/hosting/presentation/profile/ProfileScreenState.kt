package mikhail.shell.video.hosting.presentation.profile

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.User
import java.lang.Error

data class ProfileScreenState(
    val user: User? = null,
    val channels: List<Channel>? = null,
    val userError: Error? = null,
    val channelError: ChannelError? = null,
    val isLoading: Boolean = false
)
