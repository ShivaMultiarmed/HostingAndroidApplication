package mikhail.shell.video.hosting.presentation.video.edit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.FieldState

@Parcelize
sealed class VideoEditingScreenState : Parcelable {
    @Parcelize
    data object Idle : VideoEditingScreenState()
    @Parcelize
    data object Starting : VideoEditingScreenState()
    @Parcelize
    data class Editing(
        val video: VideoEditingInputState,
        val isLoading: Boolean = false
    ) : VideoEditingScreenState()
    @Parcelize
    data class Failure(
        val error: @RawValue Error
    ) : VideoEditingScreenState()
}

@Parcelize
data class VideoEditingInputState(
    val videoId: Long,
    val title: FieldState<String, TextError>,
    val cover: FieldState<EditingState<String?>, FileError>,
    val description: FieldState<String, TextError>
) : Parcelable {
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
