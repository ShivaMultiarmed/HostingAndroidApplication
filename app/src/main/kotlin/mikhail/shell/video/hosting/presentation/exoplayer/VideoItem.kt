package mikhail.shell.video.hosting.presentation.exoplayer

import android.net.Uri
import androidx.media3.common.MediaItem

data class VideoItem(
    val uri: Uri,
    val mediaItem: MediaItem
)
