package mikhail.shell.video.hosting.presentation.utils

import mikhail.shell.video.hosting.domain.errors.Error

data class FieldState<I, E: Error>(
    val value: I,
    val error: E? = null
)