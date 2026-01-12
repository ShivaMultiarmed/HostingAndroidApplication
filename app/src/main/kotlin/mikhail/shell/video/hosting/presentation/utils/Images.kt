package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun rememberAsyncImagePainter(
    model: Any?
): AsyncImagePainter {
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .size(Size.ORIGINAL)
            .build()
    )
}

@Composable
fun AsyncImagePainter.exists(): Boolean? {
    return when (state) {
        is AsyncImagePainter.State.Success -> true
        is AsyncImagePainter.State.Error -> false
        else -> null
    }
}