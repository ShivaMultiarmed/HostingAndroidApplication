package mikhail.shell.video.hosting.presentation.utils

import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State.Success

fun AsyncImagePainter.exists(): Boolean? {
    return when (state) {
        is AsyncImagePainter.State.Error -> false
        is Success -> true
        else -> null
    }
}