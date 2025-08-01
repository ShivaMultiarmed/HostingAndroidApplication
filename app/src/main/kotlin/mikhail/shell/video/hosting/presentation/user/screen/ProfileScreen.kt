package mikhail.shell.video.hosting.presentation.user.screen

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.utils.isBlank
import mikhail.shell.video.hosting.presentation.user.UserModel
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ImageViewerScreen
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
    onLogOutSuccess: () -> Unit,
    onInvite: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val orientation = LocalConfiguration.current.orientation

    var shouldShowAvatar by rememberSaveable { mutableStateOf(false) }

    val content: @Composable () -> Unit = {
        ProfileScreenContent(
            modifier = modifier,
            state = state,
            isOwner = isOwner,
            onGoToChannel = onGoToChannel,
            onPublishVideo = onPublishVideo,
            onCreateChannel = onCreateChannel,
            onRefresh = onRefresh,
            onLogOut = onLogOut,
            onInvite = onInvite,
            onShowAvatar = {
                shouldShowAvatar = true
            }
        )
    }
    Box (
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    title = stringResource(R.string.profile_title),
                    actions = if (isOwner) listOf(
                        {
                            IconButton(
                                onClick = onOpenSettings
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    contentDescription = stringResource(R.string.open_settings_button)
                                )
                            }
                        }
                    ) else null
                )
            }
        ) { padding ->
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    content()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    content()
                }
            }
        }
        if (shouldShowAvatar) {
            ImageViewerScreen(
                model = state.user!!.avatar,
                imageModifier = Modifier
                    .fillMaxWidth(0.95f)
                    .aspectRatio(1f)
                    .clip(CircleShape),
                onPopup = {
                    shouldShowAvatar = false
                }
            )
        }
    }
    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut == true) {
            onLogOutSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    isOwner: Boolean = false,
    onGoToChannel: (Long) -> Unit,
    onPublishVideo: () -> Unit,
    onCreateChannel: () -> Unit,
    onRefresh: () -> Unit,
    onLogOut: () -> Unit,
    onInvite: () -> Unit,
    onShowAvatar: () -> Unit
) {
    val context = LocalContext.current
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    val orientation = LocalConfiguration.current.orientation

    if (state.user != null && state.channels != null) {
        Column(
            modifier = Modifier.then(
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .verticalScroll(rememberScrollState())
                }
            )
        ) {
            UserDetailsSection(
                modifier = Modifier,
                user = state.user,
                onShowAvatar = onShowAvatar
            )
            if (isOwner) {
                UserActions(
                    modifier = Modifier,
                    onPublishVideo = onPublishVideo.takeIf { state.channels.isNotEmpty() },
                    onCreateChannel = onCreateChannel,
                    onLogOut = onLogOut,
                    onInvite = onInvite.takeIf { context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) }
                )
            }
        }
        Column(
            modifier = Modifier
                .then(
                    if (orientation == ORIENTATION_LANDSCAPE) {
                        Modifier
                    } else {
                        Modifier.fillMaxSize()
                    }
                )
                .padding(top = 10.dp)
        ) {
            if (state.channels.isNotEmpty()) {
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    text = stringResource(R.string.user_channels_title)
                )
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isWidthCompact) {
                                Modifier
                            } else {
                                Modifier.padding(10.dp)
                            }
                        ),
                    horizontalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp),
                    columns = GridCells.Adaptive(300.dp)
                ) {
                    items(state.channels) { channel ->
                        ChannelSnippet(
                            modifier = Modifier.then(
                                if (isWidthCompact) {
                                    Modifier
                                } else {
                                    Modifier.clip(RoundedCornerShape(15.dp))
                                }
                            ), channel = channel, onClick = {
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
                        text = stringResource(R.string.user_channels_empty_message)
                    )
                }
            }
        }
    } else if (state.channelError != null || state.userError != null) {
        ErrorComponent(
            modifier = modifier.fillMaxSize(), onRetry = onRefresh
        )
    } else {
        LoadingComponent(
            modifier = modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun UserDetailsSection(
    modifier: Modifier = Modifier,
    user: UserModel,
    onShowAvatar: () -> Unit
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    var avatarExists by rememberSaveable { mutableStateOf(null as Boolean?) }
    val avatar: @Composable () -> Unit = {
        AsyncImage(
            model = user.avatar,
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(R.string.profile_avatar_hint),
            modifier = Modifier
                .padding(top = 10.dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(
                    enabled = avatarExists == true,
                    onClick = onShowAvatar
                ),
            onSuccess = {
                avatarExists = true
            },
            onError = {
                avatarExists = false
            }
        )
    }
    val nick: @Composable () -> Unit = {
        Text(
            modifier = Modifier
                .padding(top = 5.dp),
            text = user.nick,
            fontSize = 16.sp
        )
    }
    val userTextDetails: @Composable () -> Unit = {
        UserTextDetails(
            modifier = Modifier,
            user = user
        )
    }
    if (isCompact) {
        Column(
            modifier = modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            avatar()
            nick()
            userTextDetails()
        }
    } else {
        Column(
            modifier = modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            nick()
            avatar()
            userTextDetails()
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun UserTextDetails(
    modifier: Modifier = Modifier,
    user: UserModel,
) {
    var showMore by rememberSaveable { mutableStateOf(false) }
    val showMoreButton: @Composable () -> Unit = {
        IconButton(
            onClick = {
                showMore = !showMore
            },
            modifier = Modifier.size(18.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp), imageVector = when (showMore) {
                    false -> Icons.Rounded.KeyboardArrowDown
                    true -> Icons.Rounded.KeyboardArrowUp
                },
                contentDescription = stringResource(R.string.more_button)
            )
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                user.name?.let {
                    UserDetail(
                        text = it
                    )
                }
                val contacts = arrayOf(user.email, user.tel).filterNotNull().joinToString(" ")
                AnimatedVisibility (
                    visible = showMore,
                    enter = expandVertically(
                        tween(
                            durationMillis = 300
                        )
                    ),
                    exit = shrinkVertically(
                        tween (
                            durationMillis = 300
                        )
                    )
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserDetail(contacts)
                        user.bio?.let {
                            UserDetail(it)
                        }
                    }
                }
            }
        }
        showMoreButton()
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
fun UserActions(
    modifier: Modifier = Modifier,
    onPublishVideo: (() -> Unit)? = null,
    onCreateChannel: () -> Unit,
    onLogOut: () -> Unit,
    onInvite: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .padding(bottom = 10.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onPublishVideo != null) {
            ActionButton(
                text = stringResource(R.string.upload_video_button),
                onClick = onPublishVideo
            )
        }
        ActionButton(
            text = stringResource(R.string.create_channel_button),
            onClick = onCreateChannel
        )
        if (onInvite != null) {
            ActionButton(
                text = stringResource(R.string.invite_button),
                onClick = onInvite
            )
        }
        var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }
        ActionButton(
            text = stringResource(R.string.sign_out_button),
            onClick = {
                isLogoutDialogVisible = true
            }
        )
        if (isLogoutDialogVisible) {
            Dialog(
                onSubmit = onLogOut,
                onDismiss = {
                    isLogoutDialogVisible = false
                },
                dialogTitle = stringResource(R.string.sign_out_warning_title),
                dialogDescription = stringResource(R.string.sign_out_warning_message)
            )
        }
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
                ), channels = listOf()
            ),
            isOwner = true,
            onGoToChannel = {},
            onPublishVideo = {},
            onCreateChannel = {},
            onRefresh = {},
            onLogOut = {},
            onLogOutSuccess = {},
            onInvite = {},
            onOpenSettings = {}
        )
    }
}

@Composable
fun ChannelSnippet(
    modifier: Modifier = Modifier, channel: Channel, onClick: (Long) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
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
                    text = channel.subscribers.toFullSubscribers(context),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

