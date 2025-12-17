package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ImageViewerArea(
    modifier: Modifier = Modifier,
    model: String? = null,
    onPopup: () -> Unit = {}
) {
    Column (
        modifier = modifier
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
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .aspectRatio(1f)
                    .clip(CircleShape),
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}