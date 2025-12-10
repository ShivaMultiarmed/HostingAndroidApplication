package mikhail.shell.video.hosting.presentation.utils

import mikhail.shell.video.hosting.domain.errors.Error

data class FieldState<I, E: Error>(
    val value: I,
    val initial: I = value,
    val error: E? = null
)

sealed class EditingState {
    data class Keeping<I>(val value: I) : EditingState()
    data object Removing : EditingState()
    data class Editing<I>(val value: I) : EditingState()
}