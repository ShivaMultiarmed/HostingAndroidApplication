package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel

data class UploadVideoScreenState(
    val channels: List<Channel>? = null,
    val videoValidationSuccess: Boolean = false,
    val areChannelsLoading: Boolean = false,
    val channelsLoadingError: Error? = null,
    val videoEditingError: Error? = null
)