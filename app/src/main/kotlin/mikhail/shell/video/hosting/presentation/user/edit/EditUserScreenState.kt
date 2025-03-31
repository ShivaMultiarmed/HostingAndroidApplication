package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.GetUserError
import mikhail.shell.video.hosting.domain.errors.RemoveUserError
import mikhail.shell.video.hosting.presentation.user.UserModel

data class EditUserScreenState(
    val initialUser: UserModel? = null,
    val isInitializing: Boolean = false,
    val getUserError: GetUserError? = null,
    val editedUser: UserModel? = null,
    val isEditing: Boolean = false,
    val editUserError: CompoundError<EditUserError>? = null,
    val isRemovalConfirmed: Boolean? = null,
    val isRemoving: Boolean = false,
    val removeUserError: RemoveUserError? = null
)
