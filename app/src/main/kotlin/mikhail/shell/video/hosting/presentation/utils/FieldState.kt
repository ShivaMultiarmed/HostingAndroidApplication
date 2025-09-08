package mikhail.shell.video.hosting.presentation.utils

import mikhail.shell.video.hosting.domain.errors.Error

// TODO: design adjustments if needed
data class FieldState<I, E: Error>(
    val value: I,
    val error: E? = null,
    val isTyping: Boolean = false
)