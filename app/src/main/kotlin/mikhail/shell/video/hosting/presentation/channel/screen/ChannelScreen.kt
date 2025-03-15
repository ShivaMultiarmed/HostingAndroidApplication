package mikhail.shell.video.hosting.presentation.channel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.presentation.channel.screen.sections.ChannelHeader
import mikhail.shell.video.hosting.presentation.channel.screen.sections.VideoGridSection
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent


@Composable
fun ChannelScreen(
    state: ChannelScreenState,
    onRefresh: () -> Unit,
    onSubscription: (SubscriptionState) -> Unit,
    onVideoClick: (Long) -> Unit,
    onScrollToBottom: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        if (state.channel != null && state.videos != null) {
            ChannelHeader(
                channel = state.channel,
                onSubscription = onSubscription
            )
            VideoGridSection(
                videos = state.videos,
                onVideoClick = onVideoClick,
                onScrollToBottom = onScrollToBottom
            )
        } else if (state.isChannelLoading && state.areVideosLoading) {
            LoadingComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        } else {
            ErrorComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                onRetry = onRefresh
            )
        }
    }
}