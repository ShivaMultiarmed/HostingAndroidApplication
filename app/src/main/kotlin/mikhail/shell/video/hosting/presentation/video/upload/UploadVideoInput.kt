package mikhail.shell.video.hosting.presentation.video.upload

import android.net.Uri


data class UploadVideoInput(
    val title: String,
    val channelId: Long?,
    val description: String,
    val source: Uri?,
    val cover: Uri?
)