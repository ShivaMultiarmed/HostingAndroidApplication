package mikhail.shell.video.hosting.presentation.channel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.presentation.channel.screen.sections.ChannelHeader
import mikhail.shell.video.hosting.presentation.channel.screen.sections.VideoGridSection
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorDisplay
import mikhail.shell.video.hosting.presentation.utils.ImageViewerScreen
import mikhail.shell.video.hosting.presentation.utils.StartingComponent

@Composable
fun ChannelScreen(
    userId: Long,
    state: ChannelScreenState,
    onAction: (ChannelScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.channel != null) {
                var shouldShowLogo by rememberSaveable { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ChannelHeader(
                        modifier = Modifier.padding(10.dp),
                        channel = state.channel,
                        onEvent = onAction,
                        owns = userId == state.channel.ownerId,
                        onShowLogo = {
                            shouldShowLogo = true
                        }
                    )
                    if (state.videos.videos != null) {
                        VideoGridSection(
                            modifier = Modifier.fillMaxSize(),
                            videos = state.videos.videos,
                            onVideoClick = {
                                onAction(ChannelScreenAction.ChooseVideo(it))
                            },
                            onReachedBottom = {
                                onAction(ChannelScreenAction.LoadNextPart)
                            },
                            hasMore = state.videos.hasMore,
                            isStarting = state.videos.isStarting,
                            onRestart = {
                                onAction(ChannelScreenAction.RestartVideos)
                            },
                            onReload = {
                                onAction(ChannelScreenAction.LoadNextPart)
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
                                onAction(ChannelScreenAction.RestartVideos)
                            }
                        )
                    }
                    ErrorDisplay(
                        state.videos.error,
                        snackBarHostState = snackBarHostState
                    )
                }
                if (shouldShowLogo) {
                    ImageViewerScreen(
                        state.channel.logo,
                        onPopup = {
                            shouldShowLogo = false
                        },
                        imageModifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
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
                        onAction(ChannelScreenAction.RestartChannel)
                    }
                )
            }
        }
    }
}