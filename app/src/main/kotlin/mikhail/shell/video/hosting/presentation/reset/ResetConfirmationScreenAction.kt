package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationScreenAction as ScreenAction

sealed class ResetConfirmationScreenAction {
    data class PasswordChanged(val password: String): ScreenAction()
    data object PasswordFocused: ScreenAction()
    data object PasswordBlurred: ScreenAction()
    data class PasswordDuplicatedChanged(val passwordDuplicate: String): ScreenAction()
    data object PasswordDuplicatedFocused: ScreenAction()
    data object PasswordDuplicatedBlurred: ScreenAction()
    data object Submit: ScreenAction()
}