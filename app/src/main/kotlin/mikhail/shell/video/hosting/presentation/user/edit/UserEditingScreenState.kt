package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error

sealed class UserEditingScreenState {
    data object Loading: UserEditingScreenState()
    data class Editing(
        val initialUser: EditUserUi,
        val editedUser: UserEditingInputState,
        val isLoading: Boolean = false,
        val error: Error? = null,
        val isRemoving: Boolean = false,
        val removingError: Error? = null
    ): UserEditingScreenState()
    data object Success: UserEditingScreenState()
    data class Failure(val error: Error): UserEditingScreenState()
    data object Removed: UserEditingScreenState()
}
