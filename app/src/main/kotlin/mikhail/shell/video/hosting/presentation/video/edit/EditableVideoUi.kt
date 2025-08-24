package mikhail.shell.video.hosting.presentation.video.edit

data class EditableVideoUi(
    val videoId: Long,
    val channelId: Long,
    val title: String,
    val cover: String?
)
