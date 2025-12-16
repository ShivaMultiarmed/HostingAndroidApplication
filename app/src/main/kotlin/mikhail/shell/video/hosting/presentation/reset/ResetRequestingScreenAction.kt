package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.presentation.reset.ResetRequestingScreenAction as ScreenAction

sealed class ResetRequestingScreenAction {
    data class UserNameChanged(val userName: String) : ScreenAction()
    data object UserNameFocused : ScreenAction()
    data object UserNameBlurred : ScreenAction()
    data object Submit : ScreenAction()
    data object Cancel : ScreenAction()
}