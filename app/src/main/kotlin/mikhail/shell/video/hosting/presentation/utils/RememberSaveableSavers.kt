package mikhail.shell.video.hosting.presentation.utils

import android.net.Uri
import androidx.compose.runtime.saveable.Saver

val uriSaver = Saver<Uri, String>(
    save = { it.toString() },
    restore = { Uri.parse(it) }
)