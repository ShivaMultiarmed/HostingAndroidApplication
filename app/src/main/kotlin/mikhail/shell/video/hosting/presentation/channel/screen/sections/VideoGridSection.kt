package mikhail.shell.video.hosting.presentation.channel.screen.sections

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.presentation.utils.reachedBottom
import mikhail.shell.video.hosting.presentation.video.VideoSnippet

@Composable
fun ColumnScope.VideoGridSection(
    videos: List<Video>,
    onVideoClick: (videoId: Long) -> Unit,
    onScrollToBottom: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val buffer = 4
    val reachedEnd by remember { derivedStateOf { gridState.reachedBottom(buffer) } }
    LazyVerticalGrid(
        modifier = Modifier
            .padding(top = 10.dp)
            .weight(1f),
        columns = GridCells.Adaptive(minSize = 300.dp),
        state = gridState
    ) {
        items(videos) {
            VideoSnippet(
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