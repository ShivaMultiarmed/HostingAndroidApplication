package mikhail.shell.video.hosting.presentation.video.upload

import android.net.Uri
import mikhail.shell.video.hosting.domain.models.File

data class UploadVideoInput(
    val title: String,
    val channelId: Long?,
    val description: String,
    val source: File?,
    val cover: File?
)
