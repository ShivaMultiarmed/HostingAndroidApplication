package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.presentation.user.models.UserUi

data class EditUserScreenState(
    val initialUser: UserUi? = null,
    val isInitializing: Boolean = false,
    val getUserError: Error? = null,
    val editedUser: UserUi? = null,
    val isEditing: Boolean = false,
    val editUserError: Error? = null,
    val isRemovalConfirmed: Boolean? = null,
    val isRemoving: Boolean = false,
    val removeUserError: Error? = null
)
