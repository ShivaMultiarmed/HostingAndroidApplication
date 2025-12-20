package mikhail.shell.video.hosting.presentation.utils

import coil.compose.AsyncImagePainter

fun AsyncImagePainter.exists(): Boolean? {
    return when (state) {
        is AsyncImagePainter.State.Error -> false
        is AsyncImagePainter.State.Success -> true
        else -> null
    }
}