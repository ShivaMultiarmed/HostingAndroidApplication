package mikhail.shell.video.hosting.presentation.channel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.presentation.channel.screen.sections.ChannelHeader
import mikhail.shell.video.hosting.presentation.channel.screen.sections.VideoGridSection


@Composable
fun ChannelScreen(
    state: ChannelScreenState,
    onChannelRefresh: () -> Unit,
    onVideosRefresh: () -> Unit,
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
        ChannelHeader(
            channel = state.channel,
            loading = state.isChannelLoading,
            onSubscription = onSubscription,
            onChannelRefresh = onChannelRefresh
        )
        VideoGridSection(
            videos = state.videos,
            loading = state.areVideosLoading,
            onVideoClick = onVideoClick,
            onVideosRefresh = onVideosRefresh,
            onScrollToBottom = onScrollToBottom
        )
    }
}