package mikhail.shell.video.hosting.presentation.channel.screen.sections

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.presentation.utils.reachedBottom
import mikhail.shell.video.hosting.presentation.video.VideoSnippet

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VideoGridSection(
    modifier: Modifier = Modifier,
    videos: List<Video>,
    onVideoClick: (videoId: Long) -> Unit,
    onScrollToBottom: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val buffer = 4
    val reachedEnd by remember { derivedStateOf { gridState.reachedBottom(buffer) } }
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    LazyVerticalGrid(
        modifier = modifier
            .then(
                if (isWidthCompact) {
                    Modifier
                } else {
                    Modifier
                        .padding(top = 10.dp)
                        .padding(horizontal = 10.dp)
                }
            ),
        columns = GridCells.Adaptive(minSize = 300.dp),
        state = gridState
    ) {
        items(videos) {
            VideoSnippet(
                modifier = Modifier
                    .then(
                        if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                            Modifier
                        } else {
                            Modifier
                                .clip(RoundedCornerShape(15.dp))
                        }
                    ),
                video = it,
                onClick = onVideoClick
            )
        }
    }
    LaunchedEffect(reachedEnd) {
        if (reachedEnd) {
            onScrollToBottom()
        }
    }
}