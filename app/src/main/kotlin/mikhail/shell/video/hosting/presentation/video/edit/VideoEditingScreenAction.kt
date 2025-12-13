package mikhail.shell.video.hosting.presentation.video.edit

import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenAction as ScreenAction

sealed class VideoEditingScreenAction {
    data object Restart : ScreenAction()
    data class ChangeTitle(val title: String) : ScreenAction()
    data object FocusTitle : ScreenAction()
    data object BlurTitle : ScreenAction()
    data class ChangeCover(val cover: EditingState<String?>) : ScreenAction()
    data class ChangeDescription(val description: String) : ScreenAction()
    data object FocusDescription : ScreenAction()
    data object BlurDescription : ScreenAction()
    data object Submit : ScreenAction()
    data object Cancel : ScreenAction()
}