package mikhail.shell.video.hosting.presentation.channel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.presentation.channel.screen.sections.ChannelHeader
import mikhail.shell.video.hosting.presentation.channel.screen.sections.VideoGridSection
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ImageViewerArea
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.rememberPageableBoxState
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenState as ScreenState

@Composable
fun ChannelScreen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.channel != null) {
                var shouldShowLogo by rememberSaveable { mutableStateOf(false) }
                RestartableBox(
                    modifier = Modifier.fillMaxSize(),
                    isStarting = state.isStarting,
                    onStart = {
                        onAction(ScreenAction.RestartChannel)
                    }
                ) {
                    Column (
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ChannelHeader(
                            modifier = Modifier.padding(10.dp),
                            channel = state.channel,
                            onAction = onAction,
                            owns = state.userId == state.channel.ownerId,
                            onShowLogo = {
                                shouldShowLogo = true
                            }
                        )
                        if (state.videos.videos != null) {
                            val pageableBoxState = rememberPageableBoxState(
                                items = state.videos.videos,
                                hasMore = state.videos.hasMore,
                                isLoading = state.videos.isLoading,
                                error = state.videos.error
                            )
                            VideoGridSection(
                                modifier = Modifier.fillMaxSize(),
                                state = pageableBoxState,
                                onVideoClick = {
                                    onAction(ScreenAction.ChooseVideo(it))
                                },
                                onReachedBottom = {
                                    onAction(ScreenAction.LoadNextPart)
                                },
                                isStarting = state.videos.isStarting,
                                onRestart = {
                                    onAction(ScreenAction.RestartVideos)
                                },
                                onReload = {
                                    onAction(ScreenAction.LoadNextPart)
                                }
                            )
                        } else if (state.videos.isStarting) {
                            StartingComponent(
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (state.videos.error != null) {
                            ErrorComponent(
                                modifier = Modifier.fillMaxSize(),
                                onRetry = {
                                    onAction(ScreenAction.RestartVideos)
                                }
                            )
                        }
                    }
                    if (shouldShowLogo) {
                        ImageViewerArea(
                            modifier = Modifier.fillMaxSize(),
                            model = state.channel.logo,
                            onPopup = {
                                shouldShowLogo = false
                            }
                        )
                    }
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
                        onAction(ScreenAction.RestartChannel)
                    }
                )
            }
        }
    }
}