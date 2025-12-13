package mikhail.shell.video.hosting.presentation.video.upload

sealed class VideoUploadingScreenAction {
    data object Cancel : VideoUploadingScreenAction()
    data object Restart : VideoUploadingScreenAction()
    data class ChangeChannel(val channelId: Long?) : VideoUploadingScreenAction()
    data class ChangeTitle(val title: String) : VideoUploadingScreenAction()
    data object BlurTitle : VideoUploadingScreenAction()
    data object FocusTitle : VideoUploadingScreenAction()
    data class ChangeSource(val source: String?) : VideoUploadingScreenAction()
    data class ChangeCover(val cover: String?) : VideoUploadingScreenAction()
    data class ChangeDescription(val description: String) : VideoUploadingScreenAction()
    data object BlurDescription : VideoUploadingScreenAction()
    data object FocusDescription : VideoUploadingScreenAction()
    data object Submit : VideoUploadingScreenAction()
}