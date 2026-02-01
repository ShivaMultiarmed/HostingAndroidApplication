package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.NumericError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState as ScreenState

sealed class VideoUploadingScreenState {
    data object Idle: ScreenState()
    data object Starting: ScreenState()
    data class Failure(val error: Error): ScreenState()
    data class Editing(
        val channels: List<ChannelOptionUi>,
        val video: VideoUploadingInputState,
        val isLoading: Boolean = false
    ): ScreenState()
}

data class ChannelOptionUi(
    val channelId: Long,
    val title: String
)

data class VideoUploadingInputState(
    val title: FieldState<String, Error> = FieldState(""),
    val channelId: FieldState<Long?, NumericError> = FieldState(null),
    val source: FieldState<VideoSourceInputState, FileError> = FieldState(VideoSourceInputState()),
    val cover: FieldState<String?, FileError> = FieldState(null),
    val description: FieldState<String, TextError> = FieldState("")
)

data class VideoSourceInputState(
    val current: String? = null,
    val pending: String? = null
)