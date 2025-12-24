package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.runtime.Composable
import coil.compose.AsyncImagePainter

@Composable
fun AsyncImagePainter.exists(): Boolean? {
    return when (state) {
        is AsyncImagePainter.State.Success -> true
        is AsyncImagePainter.State.Error -> false
        else -> null
    }
}