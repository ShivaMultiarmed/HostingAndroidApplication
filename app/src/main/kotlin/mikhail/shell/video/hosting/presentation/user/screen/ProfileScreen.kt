package mikhail.shell.video.hosting.presentation.user.screen

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi
import mikhail.shell.video.hosting.presentation.user.models.UserUi
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.EmptyResultComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ImageViewerScreen
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreen(
    owns: Boolean,
    state: ProfileScreenState,
    onEvent: (ProfileScreenUiEvent) -> Unit
) {
    val orientation = LocalConfiguration.current.orientation
    var shouldShowAvatar by rememberSaveable { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    title = stringResource(R.string.profile_title),
                    actions = if (owns) listOf(
                        {
                            IconButton(
                                onClick = {
                                    onEvent(ProfileScreenUiEvent.OpenSettings)
                                }
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
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackBarHostState
                )
            }
        ) { padding ->
            if (state.user != null) {
                RestartableBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onStart = {
                        onEvent(ProfileScreenUiEvent.Restart)
                    },
                    isStarting = state.isStarting
                ) {
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ProfileScreenContent(
                                owns = owns,
                                state = state,
                                onEvent = onEvent,
                                onShowAvatar = {
                                    shouldShowAvatar = true
                                }
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ProfileScreenContent(
                                owns = owns,
                                state = state,
                                onEvent = onEvent,
                                onShowAvatar = {
                                    shouldShowAvatar = true
                                }
                            )
                        }
                    }
                }
                StandardComplexErrorHandler(
                    error = state.channelState.error,
                    snackBarHostState = snackBarHostState
                )
            } else if (state.isStarting) {
                LoadingComponent(
                    modifier = Modifier.fillMaxSize()
                )
            } else if (state.error != null) {
                ErrorComponent(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = {
                        onEvent(ProfileScreenUiEvent.Restart)
                    }
                )
            }
            StandardComplexErrorHandler(
                error = state.error,
                snackBarHostState = snackBarHostState
            )
        }
        if (state.user != null && shouldShowAvatar) {
            ImageViewerScreen(
                model = state.user.avatar,
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
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun ProfileScreenContent(
    owns: Boolean,
    state: ProfileScreenState,
    onEvent: (ProfileScreenUiEvent) -> Unit,
    onShowAvatar: () -> Unit
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    val orientation = LocalConfiguration.current.orientation
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
            user = state.user!!,
            onShowAvatar = onShowAvatar
        )
        if (owns) {
            UserActions(
                onEvent = onEvent,
                hasChannels = (state.channelState.channels?.size ?: 0) > 0
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
        if (state.channelState.channels != null) {
            if (state.channelState.channels.isNotEmpty()) {
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    text = stringResource(R.string.user_channels_title)
                )
            }
            PageableBox(
                modifier = Modifier.fillMaxSize(),
                itemComponent = {
                    ChannelSnippet(
                        modifier = Modifier.then(
                            if (isWidthCompact) {
                                Modifier
                            } else {
                                Modifier.clip(RoundedCornerShape(15.dp))
                            }
                        ),
                        channel = it,
                        onClick = {
                            onEvent(ProfileScreenUiEvent.ClickedChannel(it))
                        }
                    )
                },
                emptyComponent = {
                    EmptyResultComponent(
                        modifier = Modifier.fillMaxSize(),
                        message = stringResource(R.string.user_channels_empty_message)
                    )
                },
                items = state.channelState.channels,
                hasMore = state.channelState.hasMore,
                error = state.channelState.error.takeIf { state.channelState.hasMore },
                isLoading = state.channelState.isLoading,
                onReload = {
                    onEvent(ProfileScreenUiEvent.ReloadChannels)
                },
                onReachedBottom = {
                    onEvent(ProfileScreenUiEvent.EndReached)
                }
            )
        } else if (state.channelState.isLoading) {
            LoadingComponent(
                modifier = Modifier.fillMaxSize()
            )
        } else if (state.channelState.error != null) {
            ErrorComponent(
                modifier = Modifier.fillMaxSize(),
                onRetry = {
                    onEvent(ProfileScreenUiEvent.ReloadChannels)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun UserDetailsSection(
    user: UserUi,
    onShowAvatar: () -> Unit
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    var avatarExists by rememberSaveable { mutableStateOf(null as Boolean?) }
    val avatar = @Composable {
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
    val nick = @Composable {
        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = user.nick,
            fontSize = 16.sp
        )
    }
    Column(
        modifier = Modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isCompact) {
            avatar()
            nick()
        } else {
            nick()
            avatar()
        }
        UserTextDetails(
            modifier = Modifier,
            user = user
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun UserTextDetails(
    modifier: Modifier = Modifier,
    user: UserUi,
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
                AnimatedVisibility(
                    visible = showMore,
                    enter = expandVertically(
                        tween(
                            durationMillis = 300
                        )
                    ),
                    exit = shrinkVertically(
                        tween(
                            durationMillis = 300
                        )
                    )
                ) {
                    Column(
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
private fun UserDetail(text: String) {
    Text(
        modifier = Modifier.padding(top = 5.dp),
        text = text,
        fontSize = 12.sp
    )
}

@Composable
private fun UserActions(
    modifier: Modifier = Modifier,
    hasChannels: Boolean,
    onEvent: (ProfileScreenUiEvent) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .padding(bottom = 10.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (hasChannels) {
            ActionButton(
                text = stringResource(R.string.upload_video_button),
                onClick = {
                    onEvent(ProfileScreenUiEvent.PublishVideo)
                }
            )
        }
        ActionButton(
            text = stringResource(R.string.create_channel_button),
            onClick = {
                onEvent(ProfileScreenUiEvent.CreateChannel)
            }
        )
        ActionButton(
            text = stringResource(R.string.invite_button),
            onClick = {
                onEvent(ProfileScreenUiEvent.Invite)
            }
        )
        var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }
        ActionButton(
            text = stringResource(R.string.sign_out_button),
            onClick = {
                isLogoutDialogVisible = true
            }
        )
        if (isLogoutDialogVisible) {
            Dialog(
                onSubmit = {
                    onEvent(ProfileScreenUiEvent.SignOut)
                },
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
fun ChannelSnippet(
    modifier: Modifier = Modifier,
    channel: ChannelUi,
    onClick: (Long) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick(channel.channelId)
            }
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = channel.logo,
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
                val alias = channel.alias ?: channel.channelId
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

