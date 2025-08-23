package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error

sealed class UserEditingScreenState(
    val editUserError: Error? = null,
    val isRemovalConfirmed: Boolean? = null,
    val isRemoving: Boolean = false,
    val removeUserError: Error? = null
) {
    data object Loading: UserEditingScreenState()
    data class Editing(
        val initialUser: EditUserUi,
        val isLoading: Boolean = false,
    ): UserEditingScreenState()
    data object Success: UserEditingScreenState()
    data class Failure(val error: Error): UserEditingScreenState()
}
