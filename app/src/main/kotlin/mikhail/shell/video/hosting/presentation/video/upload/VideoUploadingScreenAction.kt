package mikhail.shell.video.hosting.presentation.video.upload

typealias Action = VideoUploadingScreenAction

sealed class VideoUploadingScreenAction {
    data object Cancel : VideoUploadingScreenAction()
    data object Restart : VideoUploadingScreenAction()
    data class ChannelChanged(val channelId: Long?) : VideoUploadingScreenAction()
    data class TitleChanged(val title: String) : VideoUploadingScreenAction()
    data object TitleBlurred : VideoUploadingScreenAction()
    data object TitleFocused : VideoUploadingScreenAction()
    data class SourceChanged(val source: String?) : VideoUploadingScreenAction()
    data class CoverChanged(val cover: String?) : VideoUploadingScreenAction()
    data class DescriptionChanged(val description: String) : VideoUploadingScreenAction()
    data object DescriptionBlurred : VideoUploadingScreenAction()
    data object DescriptionFocused : VideoUploadingScreenAction()
    data object Submit : VideoUploadingScreenAction()
}