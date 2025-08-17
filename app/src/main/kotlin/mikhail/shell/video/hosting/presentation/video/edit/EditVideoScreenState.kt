package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.domain.errors.Error

data class EditVideoScreenState(
    val initialVideo: EditVideoUi? = null,
    val initialVideoError: Error? = null,
    val editVideoError: Error? = null,
    val editConfirmed: Boolean = false,
    val isLoading: Boolean = false
)