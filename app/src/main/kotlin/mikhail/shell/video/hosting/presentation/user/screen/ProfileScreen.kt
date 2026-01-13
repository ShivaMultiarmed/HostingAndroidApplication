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
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.presentation.channel.models.ChannelUi
import mikhail.shell.video.hosting.presentation.user.models.UserUi
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.EmptyComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ImageViewerArea
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.rememberPageableBoxState
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreen(
    owns: Boolean,
    state: ProfileScreenState,
    onAction: (ProfileScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val orientation = LocalConfiguration.current.orientation
    var shouldShowAvatar by rememberSaveable { mutableStateOf(false) }
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
                    actions = when {
                        owns -> listOf(
                            {
                                IconButton(
                                    onClick = {
                                        onAction(ProfileScreenAction.OpenSettings)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        contentDescription = stringResource(R.string.open_settings_button)
                                    )
                                }
                            }
                        )
                        else -> null
                    }
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
                        onAction(ProfileScreenAction.RestartProfile)
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
                                onAction = onAction,
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
                                onAction = onAction,
                                onShowAvatar = {
                                    shouldShowAvatar = true
                                }
                            )
                        }
                    }
                }
            } else if (state.isStarting) {
                StartingComponent(
                    modifier = Modifier.fillMaxSize()
                )
            } else if (state.error != null) {
                ErrorComponent(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = {
                        onAction(ProfileScreenAction.RestartProfile)
                    }
                )
            }
        }
        if (state.user != null && shouldShowAvatar) {
            ImageViewerArea(
                modifier = Modifier.fillMaxSize(),
                model = state.user.avatar[ImageSize.LARGE],
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
    onAction: (ProfileScreenAction) -> Unit,
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
                onEvent = onAction,
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
            val pageableBoxState = rememberPageableBoxState(
                items = state.channelState.channels,
                hasMore = state.channelState.hasMore,
                error = state.channelState.error,
                isLoading = state.channelState.isLoading,
            )
            PageableBox(
                modifier = Modifier.fillMaxSize(),
                state = pageableBoxState,
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
                            onAction(ProfileScreenAction.ChooseChannel(it))
                        }
                    )
                },
                emptyComponent = {
                    EmptyComponent(
                        modifier = Modifier.fillMaxSize(),
                        message = stringResource(R.string.user_channels_empty_message)
                    )
                },
                onReload = {
                    onAction(ProfileScreenAction.LoadNextChannelsPart)
                },
                onReachedEnd = {
                    onAction(ProfileScreenAction.LoadNextChannelsPart)
                }
            )
        } else if (state.channelState.isLoading) {
            StartingComponent(
                modifier = Modifier.fillMaxSize()
            )
        } else if (state.channelState.error != null) {
            ErrorComponent(
                modifier = Modifier.fillMaxSize(),
                onRetry = {
                    onAction(ProfileScreenAction.LoadNextChannelsPart)
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
            model = when(windowSize.widthSizeClass) {
               WindowWidthSizeClass.Expanded -> user.avatar[ImageSize.LARGE]
               else -> user.avatar[ImageSize.MEDIUM]
            },
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
    onEvent: (ProfileScreenAction) -> Unit
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
                    onEvent(ProfileScreenAction.PublishVideo)
                }
            )
        }
        ActionButton(
            text = stringResource(R.string.create_channel_button),
            onClick = {
                onEvent(ProfileScreenAction.CreateChannel)
            }
        )
        ActionButton(
            text = stringResource(R.string.invite_button),
            onClick = {
                onEvent(ProfileScreenAction.Invite)
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
                    onEvent(ProfileScreenAction.SignOut)
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