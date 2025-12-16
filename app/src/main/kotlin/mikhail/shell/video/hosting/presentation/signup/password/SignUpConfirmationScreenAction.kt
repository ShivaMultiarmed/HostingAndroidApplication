package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationScreenAction as ScreenAction

sealed class SignUpConfirmationScreenAction {
    data class NickChanged(val nick: String) : ScreenAction()
    data object NickFocused : ScreenAction()
    data object NickBlurred : ScreenAction()
    data class PasswordChanged(val password: String) : ScreenAction()
    data object PasswordFocused : ScreenAction()
    data object PasswordBlurred : ScreenAction()
    data class PasswordDuplicateChanged(val passwordDuplicate: String) : ScreenAction()
    data object PasswordDuplicateFocused : ScreenAction()
    data object PasswordDuplicateBlurred : ScreenAction()
    data object Submit : ScreenAction()
}