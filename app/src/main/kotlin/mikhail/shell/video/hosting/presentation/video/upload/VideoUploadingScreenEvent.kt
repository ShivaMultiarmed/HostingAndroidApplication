package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias Event = VideoUploadingScreenEvent

sealed class VideoUploadingScreenEvent {
    data object NavigateBack: VideoUploadingScreenEvent()
    data object RequireAuthentication: VideoUploadingScreenEvent()
    data class Failure(val error: Error): VideoUploadingScreenEvent()
    @OptIn(ExperimentalUuidApi::class)
    data class Success(
        val tmpId: Uuid,
        val source: String
    ): VideoUploadingScreenEvent()
}