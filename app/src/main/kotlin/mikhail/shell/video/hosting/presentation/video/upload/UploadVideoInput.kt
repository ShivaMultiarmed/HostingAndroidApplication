package mikhail.shell.video.hosting.presentation.video.upload

import java.io.File


data class UploadVideoInput(
    val title: String,
    val channelId: Long?,
    val description: String,
    val source: File?,
    val cover: File?
)
