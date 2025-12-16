package mikhail.shell.video.hosting.presentation.signin.password

import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenAction as ScreenAction

sealed class SignInScreenAction {
    data class UserNameChanged(val userName: String) : ScreenAction()
    data object UserNameFocused : ScreenAction()
    data object UserNameBlurred : ScreenAction()
    data class PasswordChanged(val password: String) : ScreenAction()
    data object PasswordFocused : ScreenAction()
    data object PasswordBlurred : ScreenAction()
    data object Submit : ScreenAction()
    data object SignUp : ScreenAction()
    data object ResetPassword: ScreenAction()
}