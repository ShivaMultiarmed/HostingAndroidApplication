package mikhail.shell.video.hosting.presentation.user.screen

import android.content.res.Configuration
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import mikhail.shell.video.hosting.presentation.user.UserModel
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.Title
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
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopBar(
            title = "Профиль",
            actions = if (isOwner) listOf(
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
            ) else null
        )

        if (state.user != null && state.channels != null) {
            UserDetailsSection(
                user = state.user
            )
            if (isOwner) {
                UserActions(
                    onPublishVideo = onPublishVideo.takeIf { state.channels.isNotEmpty() },
                    onCreateChannel = onCreateChannel,
                    onLogOut = onLogOut,
                    onInvite = onInvite
                )
            }
            if (state.channels.isNotEmpty()) {
                Title(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Каналы пользователя"
                )
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
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Здесь пока нет каналов"
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
fun UserDetailsSection(
    user: UserModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = user.avatar,
            contentScale = ContentScale.Crop,
            contentDescription = "Изображение профиля",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = user.nick,
            fontSize = 16.sp
        )
        user.name?.let { UserDetail("($it)") }
        var showMore by rememberSaveable { mutableStateOf(false) }
        IconButton(
            onClick = {
                showMore = !showMore
            },
            modifier = Modifier
                .size(18.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = when(showMore) {
                    false -> Icons.Rounded.KeyboardArrowDown
                    true -> Icons.Rounded.KeyboardArrowUp
                },
                contentDescription = "Ещё информация"
            )
        }
        val contacts = arrayOf(user.email, user.tel).filterNotNull().joinToString(" ")
        if (showMore) {
            UserDetail(contacts)
            user.bio?.let {
                UserDetail(user.bio)
            }
        }
    }
}

@Composable
fun UserDetail(text: String) {
    Text(
        modifier = Modifier.padding(top = 5.dp),
        text = text,
        fontSize = 12.sp
    )
}

@Composable
fun UserActions (
    onPublishVideo: (() -> Unit)? = null,
    onCreateChannel: () -> Unit,
    onLogOut: () -> Unit,
    onInvite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .padding(bottom = 10.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onPublishVideo != null) {
            ActionButton(
                text = "Загрузить видео",
                onClick = onPublishVideo
            )
        }
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
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ProfileScreenPreviewDay() {
    VideoHostingTheme {
        ProfileScreen(
            state = ProfileScreenState(
                user = UserModel(
                    "Balance Keeper",
                    "Mikhail Shell",
                    "",
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    "+79131234567",
                    "some@email.com"
                ),
                channels = listOf()
            ),
            isOwner = true,
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

