package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.presentation.signup.password.SignUpRequestingScreenAction as ScreenAction

sealed class SignUpRequestingScreenAction {
    data class UserNameChanged(val userName: String) : ScreenAction()
    data object UserNameFocused : ScreenAction()
    data object UserNameBlurred : ScreenAction()
    data object Submit : ScreenAction()
}