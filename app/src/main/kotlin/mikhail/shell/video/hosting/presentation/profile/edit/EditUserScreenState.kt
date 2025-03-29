package mikhail.shell.video.hosting.presentation.profile.edit

import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.RemoveUserError
import mikhail.shell.video.hosting.domain.models.User

data class EditUserScreenState(
    val initialUser: User? = null,
    val editedUser: User? = null,
    val editUserError: EditUserError? = null,
    val removeConfirmed: Boolean? = null,
    val removeUserError: RemoveUserError? = null
)
