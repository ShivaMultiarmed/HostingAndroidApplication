package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.NumericError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

typealias State = VideoUploadingScreenState

sealed class VideoUploadingScreenState {
    data object Starting: VideoUploadingScreenState()
    data class Failure(val error: Error): VideoUploadingScreenState()
    data class Editing(
        val channels: List<ChannelOptionUi>,
        val video: VideoUploadingInput,
        val isLoading: Boolean = false
    ): VideoUploadingScreenState()
}

data class ChannelOptionUi(
    val channelId: Long,
    val title: String
)

data class VideoUploadingInput(
    val title: FieldState<String, Error> = FieldState(""),
    val channelId: FieldState<Long?, NumericError> = FieldState(null),
    val source: FieldState<String?, FileError> = FieldState(null),
    val cover: FieldState<String?, FileError> = FieldState(null),
    val description: FieldState<String, TextError> = FieldState("")
)