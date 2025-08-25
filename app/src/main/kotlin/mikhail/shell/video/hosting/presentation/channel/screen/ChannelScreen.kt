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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.channel.screen.sections.ChannelHeader
import mikhail.shell.video.hosting.presentation.channel.screen.sections.VideoGridSection
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ImageViewerScreen
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler

@Composable
fun ChannelScreen(
    userId: Long,
    state: ChannelScreenState,
    onEvent: (ChannelScreenUiEvent) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
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
            if (state is ChannelScreenState.Success) {
                var shouldShowAvatar by rememberSaveable { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ChannelHeader(
                        modifier = Modifier.padding(10.dp),
                        channel = state.channel,
                        onEvent = onEvent,
                        owns = userId == state.channel.channelId,
                        onShowAvatar = {
                            shouldShowAvatar = true
                        }
                    )
                    if (state.videoState.videos != null) {
                        VideoGridSection(
                            modifier = Modifier,
                            videos = state.videoState.videos,
                            onVideoClick = {
                                onEvent(ChannelScreenUiEvent.ClickVideo(it))
                            },
                            onScrollToBottom = {
                                onEvent(ChannelScreenUiEvent.ReachedBottom)
                            },
                            areAllVideosLoaded = !state.videoState.hasMore
                        )
                        StandardComplexErrorHandler(
                            error = state.videoState.error,
                            snackBarHostState = snackBarHostState,
                            notFoundMessage = stringResource(R.string.channel_not_found)
                        )
                    }
                }
                if (shouldShowAvatar) {
                    ImageViewerScreen(
                        state.channel.avatarUrl,
                        onPopup = {
                            shouldShowAvatar = false
                        },
                        imageModifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                    )
                }
            } else if (state is ChannelScreenState.Loading) {
                LoadingComponent(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                )
            } else if (state is ChannelScreenState.Failure) {
                ErrorComponent(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    onRetry = {
                        onEvent(ChannelScreenUiEvent.Reload)
                    }
                )
                StandardComplexErrorHandler(
                    error = state.error,
                    snackBarHostState = snackBarHostState,
                    notFoundMessage = stringResource(R.string.channel_not_found)
                )
            }
        }
    }
}