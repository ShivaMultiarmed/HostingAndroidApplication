package mikhail.shell.video.hosting.presentation.signup.password

import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationScreenAction as ScreenAction

sealed class SignUpVerificationScreenAction {
    data object Submit : ScreenAction()
    data class CodeChanged(val code: String) : ScreenAction()
}