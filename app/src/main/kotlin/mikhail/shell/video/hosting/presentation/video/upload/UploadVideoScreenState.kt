package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error

data class UploadVideoScreenState(
    val channels: List<ChannelOptionUi>? = null,
    val areChannelsLoading: Boolean = false,
    val loadingChannelsError: Error? = null,
    val videoValidationSuccess: Boolean = false,
    val videoValidationError: Error? = null
)

data class ChannelOptionUi(
    val channelId: Long,
    val title: String
)