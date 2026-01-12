package mikhail.shell.video.hosting.presentation.subscriptions

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.user.screen.ChannelSnippet
import mikhail.shell.video.hosting.presentation.utils.EmptyComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.rememberPageableBoxState
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenState as ScreenState

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SubscriptionsScreen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = stringResource(R.string.subscriptions_title)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        if (state.channels != null) {
            val pageableBoxState = rememberPageableBoxState(
                items = state.channels,
                isLoading = state.isLoading,
                hasMore = state.hasMore,
                error = state.error
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
                        .fillMaxSize()
                        .then(
                            if (isWidthCompact) {
                                Modifier
                            } else {
                                Modifier
                                    .padding(top = 10.dp)
                                    .padding(horizontal = 10.dp)
                            }
                        ),
                    state = pageableBoxState,
                    itemComponent = {
                        ChannelSnippet(
                            modifier = Modifier
                                .then(
                                    if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                                        Modifier
                                    } else {
                                        Modifier.clip(RoundedCornerShape(15.dp))
                                    }
                                ),
                            channel = it,
                            onClick = {
                                onAction(ScreenAction.ChooseChannel(it))
                            }
                        )
                    },
                    emptyComponent = {
                        EmptyComponent(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            message = stringResource(R.string.no_subscriptions_yet)
                        )
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        } else if (state.error != null) {
            ErrorComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                onRetry = {
                    onAction(ScreenAction.Restart)
                }
            )
        }
    }
}