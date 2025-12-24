package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.FieldState

sealed class VideoEditingScreenState {
    data object Idle : VideoEditingScreenState()
    data object Starting : VideoEditingScreenState()
    data class Editing(
        val video: VideoEditingInputState,
        val isLoading: Boolean = false
    ) : VideoEditingScreenState()
    data class Failure(val error: Error) : VideoEditingScreenState()
}

data class VideoEditingInputState(
    val videoId: Long,
    val title: FieldState<String, TextError>,
    val cover: FieldState<EditingState<String?>, FileError>,
    val description: FieldState<String, TextError>
) {
    companion object {
        fun initialize(
            videoId: Long,
            title: String,
            cover: String,
            description: String
        ) = VideoEditingInputState(
            videoId = videoId,
            title = FieldState(title),
            cover = FieldState(EditingState.Keeping(cover)),
            description = FieldState(description)
        )
    }
}
