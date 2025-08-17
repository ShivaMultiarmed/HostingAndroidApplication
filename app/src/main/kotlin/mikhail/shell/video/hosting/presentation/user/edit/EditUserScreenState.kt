package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error

data class EditUserScreenState(
    val initialUser: EditUserUi? = null,
    val isInitializing: Boolean = false,
    val getUserError: Error? = null,
    val editUserSuccess: Boolean = false,
    val isEditing: Boolean = false,
    val editUserError: Error? = null,
    val isRemovalConfirmed: Boolean? = null,
    val isRemoving: Boolean = false,
    val removeUserError: Error? = null
)
