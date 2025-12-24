package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction as ScreenAction

sealed class VideoUploadingScreenAction {
    data object Cancel : ScreenAction()
    data object Restart : ScreenAction()
    data class ChangeChannel(val channelId: Long?) : ScreenAction()
    data class ChangeTitle(val title: String) : ScreenAction()
    data object BlurTitle : ScreenAction()
    data object FocusTitle : ScreenAction()
    data class ChangeSource(val source: String?) : ScreenAction()
    data class ChangeCover(val cover: String?) : ScreenAction()
    data class ChangeDescription(val description: String) : ScreenAction()
    data object BlurDescription : ScreenAction()
    data object FocusDescription : ScreenAction()
    data class ShowPermissionLack(val message: String) : ScreenAction()
    data object Submit : ScreenAction()
}