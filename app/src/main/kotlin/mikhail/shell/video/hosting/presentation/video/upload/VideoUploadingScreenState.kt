package mikhail.shell.video.hosting.presentation.video.upload

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.NumericError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState as ScreenState

@Parcelize
sealed class VideoUploadingScreenState : Parcelable {
    @Parcelize
    data object Idle: ScreenState()
    @Parcelize
    data object Starting: ScreenState()
    @Parcelize
    data class Failure(
        val error: @RawValue Error
    ): ScreenState()
    @Parcelize
    data class Editing(
        val channels: List<ChannelOptionUi>,
        val video: VideoUploadingInputState,
        val isLoading: Boolean = false
    ): ScreenState()
}

@Parcelize
data class ChannelOptionUi(
    val channelId: Long,
    val title: String
): Parcelable

@Parcelize
data class VideoUploadingInputState(
    val title: FieldState<String, Error> = FieldState(""),
    val channelId: FieldState<Long?, NumericError> = FieldState(null),
    val source: FieldState<VideoSourceInputState, FileError> = FieldState(VideoSourceInputState()),
    val cover: FieldState<String?, FileError> = FieldState(null),
    val description: FieldState<String, TextError> = FieldState("")
): Parcelable

@Parcelize
data class VideoSourceInputState(
    val current: String? = null,
    val pending: String? = null
): Parcelable