package mikhail.shell.video.hosting.presentation.user.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.utils.isBlank
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    isOwner: Boolean = false,
    onGoToChannel: (Long) -> Unit,
    onPublishVideo: () -> Unit,
    onCreateChannel: () -> Unit,
    onRefresh: () -> Unit,
    onLogOut: () -> Unit,
    onInvite: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TopBar(
            title = "Профиль",
            actions = listOf(
                {
                    IconButton(
                        onClick = onOpenSettings
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = "Открыть настройки"
                        )
                    }
                }
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .padding(horizontal = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionButton(
                text = "Загрузить видео",
                onClick = onPublishVideo
            )
            ActionButton(
                text = "Создать канал",
                onClick = onCreateChannel
            )
            ActionButton(
                text = "Пригласить",
                onClick = onInvite
            )
            ActionButton(
                text = "Выйти",
                onClick = onLogOut
            )
        }
        if (state.channels != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(state.channels) { channel ->
                    ChannelSnippet(
                        channel = channel,
                        onClick = {
                            onGoToChannel(channel.channelId!!)
                        }
                    )
                }
            }
        } else if (state.channelError != null || state.userError != null) {
            ErrorComponent(
                modifier = modifier.fillMaxSize(),
                onRetry = onRefresh
            )
        } else {
            LoadingComponent(
                modifier = modifier.fillMaxSize()
            )
        }
    }

}

@Composable
@Preview
fun ProfileScreenPreviewDay() {
    VideoHostingTheme {
        ProfileScreen(
            state = ProfileScreenState(),
            onGoToChannel = {},
            onPublishVideo = {},
            onCreateChannel = {},
            onRefresh = {},
            onLogOut = {},
            onInvite = {},
            onOpenSettings = {}
        )
    }
}

@Composable
@Preview
fun ProfileScreenPreviewNight() {
    VideoHostingTheme {
        ProfileScreen(
            state = ProfileScreenState(),
            onGoToChannel = {},
            onPublishVideo = {},
            onCreateChannel = {},
            onRefresh = {},
            onLogOut = {},
            onInvite = {},
            onOpenSettings = {}
        )
    }
}

@Composable
fun ChannelSnippet(
    modifier: Modifier = Modifier,
    channel: Channel,
    onClick: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick(channel.channelId!!)
            }
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = channel.avatarUrl,
                contentDescription = channel.title,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = channel.title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                val alias = if (!channel.alias.isBlank()) channel.alias else channel.channelId
                Text(
                    text = "@$alias",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = channel.subscribers.toFullSubscribers(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

