package mikhail.shell.video.hosting.presentation.utils

import mikhail.shell.video.hosting.domain.errors.Error

data class FieldState<I, E: Error>(
    val value: I,
    val initial: I = value,
    val error: E? = null
)

sealed class EditingState<I> {
    data class Keeping<I>(val value: I) : EditingState<I>()
    data object Removing : EditingState<Nothing>()
    data class Editing<I>(val value: I) : EditingState<I>()
}