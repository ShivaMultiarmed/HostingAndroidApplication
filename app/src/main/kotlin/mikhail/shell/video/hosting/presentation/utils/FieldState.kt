package mikhail.shell.video.hosting.presentation.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import mikhail.shell.video.hosting.domain.errors.Error

@Parcelize
data class FieldState<I, E: Error> (
    val value: @RawValue I,
    val initial: @RawValue I = value,
    val error: @RawValue E? = null
) : Parcelable

@Parcelize
sealed class EditingState<out I> : Parcelable {
    @Parcelize
    data class Keeping<out I>(
        val value: @RawValue I
    ) : EditingState<I>()
    @Parcelize
    data object Removing : EditingState<Nothing>()
    @Parcelize
    data class Editing<out I>(
        val value: @RawValue I
    ) : EditingState<I>()
}