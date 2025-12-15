package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenAction as ScreenAction

sealed class UserEditingScreenAction {
    data class ChangeNick(val nick: String) : ScreenAction()
    data object FocusNick : ScreenAction()
    data object BlurNick : ScreenAction()
    data class ChangeName(val name: String) : ScreenAction()
    data object FocusName : ScreenAction()
    data object BlurName : ScreenAction()
    data class ChangeAvatar(val avatar: EditingState<String?>) : ScreenAction()
    data class ChangeBio(val bio: String) : ScreenAction()
    data object FocusBio : ScreenAction()
    data object BlurBio : ScreenAction()
    data class ChangeTelephone(val telephone: String) : ScreenAction()
    data object FocusTelephone : ScreenAction()
    data object BlurTelephone : ScreenAction()
    data class ChangeEmail(val email: String) : ScreenAction()
    data object FocusEmail : ScreenAction()
    data object BlurEmail : ScreenAction()
    data object Submit : ScreenAction()
    data object Cancel : ScreenAction()
    data object Restart : ScreenAction()
    data object Remove : ScreenAction()
}