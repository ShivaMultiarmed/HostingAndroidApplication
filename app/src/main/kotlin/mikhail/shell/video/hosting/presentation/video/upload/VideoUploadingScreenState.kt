package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class VideoUploadingScreenState {
    data object Loading: VideoUploadingScreenState()
    data class Editing(
        val channels: List<ChannelOptionUi>,
        val video: VideoUploadingInput,
        val isLoading: Boolean = false,
        val error: Error? = null
    ): VideoUploadingScreenState()
    data class Failure(val error: Error): VideoUploadingScreenState()
    data class Success @OptIn(ExperimentalUuidApi::class) constructor(
        val uploadId: Uuid,
        val source: String
    ): VideoUploadingScreenState()
}

data class ChannelOptionUi(
    val channelId: Long,
    val title: String
)