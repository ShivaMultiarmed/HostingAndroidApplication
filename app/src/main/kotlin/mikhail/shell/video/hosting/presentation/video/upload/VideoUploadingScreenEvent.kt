package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenEvent as ScreenEvent

sealed class VideoUploadingScreenEvent {
    data object Cancelled : ScreenEvent()
    data class Failure(val error: Error): ScreenEvent()
    @OptIn(ExperimentalUuidApi::class)
    data class Success(
        val tmpId: Uuid,
        val channelId: Long,
        val source: String
    ): ScreenEvent()
}