package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.RemoveUserError
import mikhail.shell.video.hosting.presentation.user.UserModel

data class EditUserScreenState(
    val initialUser: UserModel? = null,
    val isInitializing: Boolean = false,
    val getUserError: Error? = null,
    val editedUser: UserModel? = null,
    val isEditing: Boolean = false,
    val editUserError: Error? = null,
    val isRemovalConfirmed: Boolean? = null,
    val isRemoving: Boolean = false,
    val removeUserError: RemoveUserError? = null
)
