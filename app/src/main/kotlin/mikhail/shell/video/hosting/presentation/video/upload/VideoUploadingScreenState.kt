package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error

sealed class VideoUploadingScreenState {
    data object Loading: VideoUploadingScreenState()
    data class Editing(
        val channels: List<ChannelOptionUi>,
        val error: Error? = null
    ): VideoUploadingScreenState()
    data class Failure(val error: Error): VideoUploadingScreenState()
    data object Success: VideoUploadingScreenState()
}

data class ChannelOptionUi(
    val channelId: Long,
    val title: String
)