package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.min

@Composable
fun ImageViewerArea(
    modifier: Modifier = Modifier,
    model: String? = null,
    onPopup: () -> Unit = {}
) {
    val density = LocalDensity.current
    var logoSide by remember {
        mutableStateOf(0.dp)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0, 0, 0, 150))
            .clickable(onClick = onPopup)
            .onGloballyPositioned {
                logoSide = with(density) {
                    (0.95f * min(it.size.width, it.size.height)).toInt().toDp()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier

                .size(logoSide)
                .clip(CircleShape),
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}