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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.rememberPageableBoxState
import mikhail.shell.video.hosting.presentation.video.search.VideoWithChannelSnippet
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreenState as ScreenState

@Composable
fun RecommendationsScreen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
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
            SnackbarHost(
                hostState = snackBarHostState
            )
        }
    ) { padding ->
        if (state.videos != null) {
            val pageableBoxState = rememberPageableBoxState(
                items = state.videos,
                hasMore = state.hasMore,
                error = state.error,
                isLoading = state.isLoading,
            )
            RestartableBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onStart = {
                    onAction(ScreenAction.Restart)
                },
                isStarting = state.isStarting,
                canStart = !pageableBoxState.gridState.canScrollBackward
            ) {
                PageableBox(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = pageableBoxState,
                    itemComponent = {
                        VideoWithChannelSnippet(
                            modifier = Modifier.fillMaxWidth(),
                            videoWithChannel = it,
                            onClick = {
                                onAction(ScreenAction.ChooseVideo(it))
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
                    onReload = {
                        onAction(ScreenAction.LoadNextPart)
                    },
                    onReachedEnd = {
                        onAction(ScreenAction.LoadNextPart)
                    }
                )
            }
        } else if (state.isStarting) {
            StartingComponent(
                modifier = Modifier.fillMaxSize()
            )
        } else if (state.error != null) {
            ErrorComponent(
                modifier = Modifier.fillMaxSize(),
                onRetry = {
                    onAction(ScreenAction.Restart)
                }
            )
        }
    }
}