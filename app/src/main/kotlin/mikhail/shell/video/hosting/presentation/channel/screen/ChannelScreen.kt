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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.presentation.channel.screen.sections.ChannelHeader
import mikhail.shell.video.hosting.presentation.channel.screen.sections.VideoGridSection
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ImageViewerScreen
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent


@Composable
fun ChannelScreen(
    state: ChannelScreenState,
    onRefresh: () -> Unit,
    onSubscription: (SubscriptionState) -> Unit,
    onVideoClick: (Long) -> Unit,
    onScrollToBottom: () -> Unit,
    onEdit: (channelId: Long) -> Unit = {},
    onRemove: (channelId: Long) -> Unit = {},
    owns: Boolean = false
) {
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(padding)
            ) {
                if (state.channel != null && state.videos != null) {
                    var shouldShowAvatar by rememberSaveable { mutableStateOf(false) }
                    Column (
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ChannelHeader(
                            modifier = Modifier.padding(10.dp),
                            channel = state.channel,
                            onSubscription = onSubscription,
                            onEdit = onEdit,
                            onRemove = onRemove,
                            owns = owns,
                            onShowAvatar = {
                                shouldShowAvatar = true
                            }
                        )
                        VideoGridSection(
                            modifier = Modifier,
                            videos = state.videos,
                            onVideoClick = onVideoClick,
                            onScrollToBottom = onScrollToBottom
                        )
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
                } else if (state.isChannelLoading || state.areVideosLoading) {
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
    }
}