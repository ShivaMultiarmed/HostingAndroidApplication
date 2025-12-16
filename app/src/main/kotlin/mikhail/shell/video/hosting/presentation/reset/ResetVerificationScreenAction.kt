package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.presentation.reset.ResetVerificationScreenAction as ScreenAction

sealed class ResetVerificationScreenAction {
    data class CodeChanged(val code: String): ScreenAction()
}