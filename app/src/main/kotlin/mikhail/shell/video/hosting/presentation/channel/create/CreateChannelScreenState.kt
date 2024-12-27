package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Channel

data class CreateChannelScreenState(
    val channel: Channel? = null,
    val error: CompoundError<ChannelCreationError>? = null,
    val isLoading: Boolean = false
)