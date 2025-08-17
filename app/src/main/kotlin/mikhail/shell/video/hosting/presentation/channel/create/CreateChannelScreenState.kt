package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.errors.Error

data class CreateChannelScreenState(
    val channelId: Long? = null,
    val error: Error? = null,
    val isLoading: Boolean = false
)