package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class VideoUploadingScreenEvent {
    data object Cancelled : VideoUploadingScreenEvent()
    data class Failure(val error: Error): VideoUploadingScreenEvent()
    @OptIn(ExperimentalUuidApi::class)
    data class Success(
        val tmpId: Uuid,
        val channelId: Long,
        val source: String
    ): VideoUploadingScreenEvent()
}