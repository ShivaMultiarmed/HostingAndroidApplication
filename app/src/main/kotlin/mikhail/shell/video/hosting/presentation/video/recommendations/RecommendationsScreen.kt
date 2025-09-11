package mikhail.shell.video.hosting.presentation.video.recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorDisplay
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.ReloadableBox
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.video.search.VideoWithChannelSnippet

@Composable
fun RecommendationsScreen(
    state: RecommendationsScreenState,
    onEvent: (RecommendationsScreenUiEvent) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = stringResource(R.string.recommendations_title)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        if (state.videos != null) {
            ReloadableBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onLaunch = {
                    onEvent(RecommendationsScreenUiEvent.DraggedDown)
                },
                isLoading = state.isStarting,
            ) {
                PageableBox(
                    modifier = Modifier
                        .fillMaxSize(),
                    itemComponent = {
                        VideoWithChannelSnippet(
                            modifier = Modifier.fillMaxWidth(),
                            videoWithChannel = it,
                            onClick = {
                                onEvent(RecommendationsScreenUiEvent.ClickedVideo(it))
                            }
                        )
                    },
                    emptyComponent = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_recommendations_yet),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    items = state.videos,
                    hasMore = state.hasMore,
                    error = state.error,
                    isLoading = state.isLoading,
                    onReload = {
                        onEvent(RecommendationsScreenUiEvent.BottomReached)
                    },
                    onReachedBottom = {
                        onEvent(RecommendationsScreenUiEvent.BottomReached)
                    }
                )
            }
        } else if (state.isStarting) {
            LoadingComponent(
                modifier = Modifier.fillMaxSize()
            )
        } else if (state.error != null) {
            ErrorComponent(
                modifier = Modifier.fillMaxSize(),
                onRetry = {
                    onEvent(RecommendationsScreenUiEvent.Reload)
                }
            )
        }
        ErrorDisplay(
            error = state.error,
            snackBarHostState = snackBarHostState
        )
    }
}