package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ImageViewerScreen(
    model: String? = null,
    imageModifier: Modifier = Modifier,
    onPopup: () -> Unit = {}
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0, 0, 0, 150))
            .clickable(onClick = onPopup),
    ) {
        Box (
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                modifier = imageModifier,
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}