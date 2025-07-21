package mikhail.shell.video.hosting.presentation.video.recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.reachedBottom
import mikhail.shell.video.hosting.presentation.video.search.VideoWithChannelSnippet

@Composable
fun VideoRecommendationsScreen(
    state: VideoRecommendationsScreenState,
    onLoadVideosPart: () -> Unit,
    onVideoClick: (videoId: Long) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = stringResource(R.string.recommendations_title)
            )
        }
    ) { padding ->
        val lazyGridState = rememberLazyGridState()
        val reachedBottom by remember {
            derivedStateOf {
                lazyGridState.reachedBottom(buffer = 4)
            }
        }
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            columns = GridCells.Adaptive(300.dp),
            state = lazyGridState,
            verticalArrangement = if (state.videos.isNullOrEmpty()) Arrangement.Center else Arrangement.spacedBy(
                10.dp
            )
        ) {
            if (state.videos != null) {
                if (state.videos.isNotEmpty()) {
                    items(state.videos) {
                        VideoWithChannelSnippet(
                            modifier = Modifier.fillMaxWidth(),
                            videoWithChannel = it,
                            onClick = onVideoClick
                        )
                    }
                } else {
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_recommendations_yet),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                val loadModifier = Modifier.then(
                    if (state.videos == null) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    }
                )
                if (state.areVideosLoading) {
                    LoadingComponent(
                        modifier = loadModifier
                    )
                } else if (state.videosLoadingError != null) {
                    ErrorComponent(
                        modifier = loadModifier,
                        onRetry = onLoadVideosPart
                    )
                }
            }
        }
        LaunchedEffect(reachedBottom) {
            if (reachedBottom && !state.areAllVideosLoaded) {
                onLoadVideosPart()
            }
        }
    }
}