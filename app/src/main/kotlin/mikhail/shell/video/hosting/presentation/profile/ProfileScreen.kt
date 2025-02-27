package mikhail.shell.video.hosting.presentation.profile

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
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.utils.isBlank
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.Toggle
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers
import mikhail.shell.video.hosting.ui.theme.Theme
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import mikhail.shell.video.hosting.ui.theme.getThemeSelected
import mikhail.shell.video.hosting.ui.theme.setTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    onGoToChannel: (Long) -> Unit,
    onPublishVideo: () -> Unit,
    onCreateChannel: () -> Unit,
    onRefresh: () -> Unit,
    onLogOut: () -> Unit
) {
    val context = LocalContext.current
    if (state.channels != null) {
        Column (
            modifier = modifier
                .fillMaxSize()
                .padding(top = 10.dp)

        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
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
                    text = "Выйти",
                    onClick = onLogOut
                )
            }
            var selectedTheme by remember { mutableStateOf(context.getThemeSelected()) }
            Box (
                modifier = Modifier.fillMaxWidth()
            ) {
                Toggle(
                    key = selectedTheme,
                    values = mapOf(
                        Theme.LIGHT to Icons.Rounded.WbSunny,
                        Theme.BY_TIME to Icons.Rounded.Timelapse,
                        Theme.DARK to Icons.Rounded.ModeNight
                    ),
                    onValueChanged = {
                        context.setTheme(it)
                        selectedTheme = it
                    }
                )
            }
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

@Composable
@Preview
fun ProfileScreenPreviewDay() {
    VideoHostingTheme {
        ProfileScreen(
            state = ProfileScreenState(),
            onGoToChannel = {},
            onRefresh = {},
            onPublishVideo = {},
            onCreateChannel = {},
            onLogOut = {}
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
            onRefresh = {},
            onPublishVideo = {},
            onCreateChannel = {},
            onLogOut = {}
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

