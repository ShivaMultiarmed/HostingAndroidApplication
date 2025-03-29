package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.GetUserError
import mikhail.shell.video.hosting.domain.errors.RemoveUserError
import mikhail.shell.video.hosting.domain.models.User

data class EditUserScreenState(
    val initialUser: User? = null,
    val isInitializing: Boolean = false,
    val getUserError: GetUserError? = null,
    val editedUser: User? = null,
    val isEditing: Boolean = false,
    val editUserError: CompoundError<EditUserError>? = null,
    val isRemovalConfirmed: Boolean? = null,
    val isRemoving: Boolean = false,
    val removeUserError: RemoveUserError? = null
)
