package mikhail.shell.video.hosting.presentation.video.screen

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.window.layout.WindowMetricsCalculator
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Liking.DISLIKED
import mikhail.shell.video.hosting.domain.models.Liking.LIKED
import mikhail.shell.video.hosting.domain.models.Liking.NONE
import mikhail.shell.video.hosting.domain.models.Subscription.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.Subscription.SUBSCRIBED
import mikhail.shell.video.hosting.presentation.exoplayer.LocalPlayerState
import mikhail.shell.video.hosting.presentation.exoplayer.PlayerComponent
import mikhail.shell.video.hosting.presentation.models.CommentUi
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.EditButton
import mikhail.shell.video.hosting.presentation.utils.EmptyComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.PrimaryToggleButton
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.toRoundString
import mikhail.shell.video.hosting.presentation.utils.toSubscribers
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.ui.theme.Black
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    state: VideoScreenState,
    player: Player,
    userId: Long,
    onAction: (VideoScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val activity = LocalActivity.current!!
    val lifecycleOwner = LocalLifecycleOwner.current
    val playerState = LocalPlayerState.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.imePadding(),
                hostState = snackBarHostState
            )
        }
    ) { padding ->
        if (state.video != null) {
            var isFullScreen by rememberSaveable { mutableStateOf(false) }
            var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
            val scrollState = rememberScrollState()
            val orientation = LocalConfiguration.current.orientation
            val isSmallWindow = rememberIsSmallWindow()
            val targetOrientation = remember(isFullScreen, isSmallWindow) {
                if (isSmallWindow) {
                    if (isFullScreen && aspectRatio >= 1f) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
            val isFullScreenReached =
                remember(isFullScreen, orientation, targetOrientation, isSmallWindow) {
                    if (isSmallWindow) {
                        isFullScreen && targetOrientation == when (orientation) {
                            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    } else {
                        isFullScreen
                    }
                }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isFullScreenReached) {
                                Modifier.fillMaxHeight()
                            } else {
                                Modifier
                            }
                        )
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerComponent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isFullScreenReached) {
                                    Modifier.fillMaxHeight()
                                } else {
                                    if (isSmallWindow) {
                                        try {
                                            Modifier.aspectRatio(if (aspectRatio < 1f) 16f / 9 else aspectRatio)
                                        } catch (_: IllegalArgumentException) {
                                            Modifier.aspectRatio(16f / 9)
                                        }
                                    } else {
                                        Modifier.fillMaxHeight(0.5f)
                                    }
                                }
                            ),
                        player = player,
                        onRatioObtained = {
                            aspectRatio = it
                        },
                        isFullScreen = isFullScreen,
                        onFullscreen = {
                            isFullScreen = it
                        }
                    )
                }
                LaunchedEffect(isFullScreenReached) {
                    playerState.value = playerState.value.copy(fullScreen = isFullScreenReached)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val window = activity.window
                        WindowCompat.setDecorFitsSystemWindows(window, !isFullScreenReached)
                        if (isFullScreenReached) {
                            window.insetsController?.let {
                                it.hide(WindowInsetsCompat.Type.systemBars())
                                it.systemBarsBehavior =
                                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            }
                        } else {
                            window.insetsController?.show(WindowInsetsCompat.Type.systemBars())
                        }
                    }
                }
                LaunchedEffect(targetOrientation) {
                    if (activity.requestedOrientation != targetOrientation) {
                        activity.requestedOrientation = targetOrientation
                    }
                }
                DisposableEffect(Unit) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) {
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                if (!isFullScreenReached) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Black)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp
                                )
                            )
                            .background(MaterialTheme.colorScheme.background)
                            .padding(12.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = state.video.videoTitle,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 20.sp,
                                maxLines = 2,
                                lineHeight = 22.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.video.views.toViews(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                modifier = Modifier.size(12.dp),
                                imageVector = Icons.Rounded.Visibility,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = state.video.views.toViews()
                            )
                            Text(
                                text = state.video.dateTime.toPresentation(context),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                            if (userId == state.video.ownerId) {
                                var isDeletingDialogOpen by rememberSaveable { mutableStateOf(false) }
                                var isAdvancedDialogOpen by rememberSaveable { mutableStateOf(false) }
                                Box {
                                    EditButton(
                                        modifier = Modifier.size(22.dp),
                                        imageVector = Icons.Rounded.MoreVert,
                                        onClick = {
                                            isAdvancedDialogOpen = true
                                        }
                                    )
                                    if (isAdvancedDialogOpen) {
                                        ContextMenu(
                                            modifier = Modifier,
                                            isExpanded = true,
                                            menuItems = listOf(
                                                MenuItem(
                                                    title = stringResource(R.string.video_edit_button),
                                                    onClick = {
                                                        onAction(VideoScreenAction.Edit)
                                                    }
                                                ),
                                                MenuItem(
                                                    title = stringResource(R.string.video_delete_button),
                                                    onClick = {
                                                        isDeletingDialogOpen = true
                                                    }
                                                )
                                            ),
                                            onDismiss = {
                                                isAdvancedDialogOpen = false
                                            }
                                        )
                                    }
                                }
                                if (isDeletingDialogOpen) {
                                    Dialog(
                                        onSubmit = {
                                            onAction(VideoScreenAction.Remove)
                                        },
                                        onDismiss = {
                                            isDeletingDialogOpen = false
                                        },
                                        dialogTitle = stringResource(R.string.video_delete_warning_title),
                                        dialogDescription = stringResource(R.string.video_delete_warning_message)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        onAction(VideoScreenAction.OpenChannel)
                                    }
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = state.video.channelLogo,
                                    contentDescription = stringResource(R.string.channel_link),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                )
                                Text(
                                    text = state.video.channelTitle,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 13.dp),
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = state.video.subscribers.toSubscribers(),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(end = 5.dp)
                                )
                                Icon(
                                    modifier = Modifier.size(14.dp),
                                    imageVector = Icons.Rounded.Person,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = state.video.subscribers.toSubscribers()
                                )
                            }
                            PrimaryToggleButton(
                                toggled = state.video.subscription == SUBSCRIBED,
                                onClick = {
                                    val subscriptionState = when (state.video.subscription) {
                                        SUBSCRIBED -> NOT_SUBSCRIBED
                                        else -> SUBSCRIBED
                                    }
                                    onAction(VideoScreenAction.Subscribe(subscriptionState))
                                },
                                toggledOffText = stringResource(R.string.subscribe_button),
                                toggledOnText = stringResource(R.string.unsubscribe_button)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val likeVector = when (state.video.liking) {
                                LIKED -> Icons.Rounded.ThumbUp
                                else -> Icons.Outlined.ThumbUp
                            }
                            ActionButton(
                                icon = likeVector,
                                text = state.video.likes.toRoundString(),
                                onClick = {
                                    onAction(VideoScreenAction.Like(if (state.video.liking != LIKED) LIKED else NONE))
                                }
                            )
                            val dislikeVector = when (state.video.liking) {
                                DISLIKED -> Icons.Rounded.ThumbDown
                                else -> Icons.Outlined.ThumbDown
                            }
                            ActionButton(
                                icon = dislikeVector,
                                text = state.video.dislikes.toRoundString(),
                                onClick = {
                                    onAction(VideoScreenAction.Like(if (state.video.liking != DISLIKED) DISLIKED else NONE))
                                }
                            )
                            ActionButton(
                                icon = Icons.Rounded.Share,
                                text = stringResource(R.string.video_share),
                                onClick = {
                                    onAction(VideoScreenAction.Share)
                                }
                            )
                            ActionButton(
                                icon = Icons.Outlined.Download,
                                text = stringResource(R.string.video_download_button),
                                onClick = {
                                    onAction(VideoScreenAction.DownLoad)
                                }
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .clickable {
                                    coroutineScope.launch {
                                        sheetState.show()
                                    }
                                }
                                .padding(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.comments_title),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
                                val leaveCommentBg = tertiaryContainer.copy(
                                    red = tertiaryContainer.red - 10f / 255,
                                    green = tertiaryContainer.green - 10f / 255,
                                    blue = tertiaryContainer.blue - 10f / 255
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 10.dp)
                                        .clip(CircleShape)
                                        .background(leaveCommentBg)
                                        .padding(vertical = 3.dp, horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.comments_leave_hint),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                        }
                    }
                }
            }
            if (sheetState.isVisible) {
                CommentsBottomSheet(
                    sheetState = sheetState,
                    commentsState = state.commentsState,
                    userId = userId,
                    snackBarHostState = snackBarHostState,
                    onAction = onAction
                )
            }
            LaunchedEffect(sheetState.isVisible) {
                if (sheetState.isVisible) {
                    onAction(VideoScreenAction.OpenComments)
                } else {
                    onAction(VideoScreenAction.CloseComments)
                }
            }
        } else if (state.isStarting) {
            StartingComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        } else if (state.error != null) {
            ErrorComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                onRetry = {
                    onAction(VideoScreenAction.Restart)
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    userId: Long,
    sheetState: SheetState,
    snackBarHostState: SnackbarHostState,
    commentsState: CommentsState,
    onAction: (VideoScreenAction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
            }
            onAction(VideoScreenAction.CloseComments)
        },
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(10.dp),
        ) {
            if (commentsState.comments != null) {
                PageableBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    itemComponent = {
                        CommentBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            owns = it.userId == userId,
                            onEvent = onAction,
                            comment = it,
                        )
                    },
                    emptyComponent = {
                        EmptyComponent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            message = stringResource(R.string.comments_empty_message)
                        )
                    },
                    items = commentsState.comments,
                    hasMore = commentsState.hasMore,
                    error = commentsState.error,
                    isLoading = commentsState.isLoading,
                    onReachedBottom = {
                        onAction(VideoScreenAction.LoadNextCommentsPart)
                    },
                    onReload = {
                        onAction(VideoScreenAction.LoadNextCommentsPart)
                    }
                )
            } else if (commentsState.isStarting) {
                StartingComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else if (commentsState.error != null) {
                ErrorComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onRetry = {
                        onAction(VideoScreenAction.RestartComments)
                    }
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            CommentForm(
                text = commentsState.comment.text.value,
                onAction = onAction,
                error = commentsState.error
            )
        }
    }
}

@Composable
private fun CommentBox(
    modifier: Modifier = Modifier,
    owns: Boolean,
    comment: CommentUi,
    onEvent: (VideoScreenAction) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        var isMenuVisible by rememberSaveable { mutableStateOf(false) }
        ContextMenu(
            isExpanded = isMenuVisible,
            menuItems = listOf(
                MenuItem(
                    title = stringResource(R.string.comment_edit_button),
                    onClick = {
                        onEvent(
                            VideoScreenAction.EditComment(comment.commentId)
                        )
                        isMenuVisible = false
                    }
                ),
                MenuItem(
                    title = stringResource(R.string.comment_delete_button),
                    onClick = {
                        onEvent(VideoScreenAction.RemoveComment(comment.commentId))
                        isMenuVisible = false
                    }
                )
            ),
            onDismiss = {
                isMenuVisible = false
            }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .clickable {
                        onEvent(VideoScreenAction.OpenProfile(comment.userId))
                    },
                model = comment.avatar,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = comment.nick + " - " + comment.dateTime.toPresentation(context),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (owns) {
                        IconButton(
                            modifier = Modifier.size(20.dp),
                            onClick = {
                                isMenuVisible = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = null
                            )
                        }
                    }
                }
                Text(
                    text = comment.text,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun CommentForm(
    text: String,
    onAction: (VideoScreenAction) -> Unit,
    error: Error?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .border(
                    width = 1.dp,
                    color = when (error is TextError) {
                        false -> Color.Transparent
                        true -> MaterialTheme.colorScheme.error
                    },
                    shape = RoundedCornerShape(5.dp)
                )
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .weight(1f),
            value = text,
            maxLines = 100,
            onValueChange = {
                onAction(VideoScreenAction.ChangeCommentText(it))
            },
            textStyle = TextStyle(
                fontSize = 16.sp
            ),
            decorationBox = { innerText ->
                Box(
                    modifier = Modifier.padding(5.dp)
                ) {
                    if (text.isNotEmpty()) {
                        innerText()
                    } else {
                        Text(
                            fontSize = 16.sp,
                            text = stringResource(R.string.comments_leave_hint)
                        )
                    }
                }
            }
        )
        PrimaryProgressButton(
            enabled = text.isNotEmpty(),
            onClick = {
                onAction(VideoScreenAction.SubmitComment)
            },
            icon = Icons.AutoMirrored.Rounded.Send
        )
    }
}

fun LocalDateTime.toPresentation(
    context: Context,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val now = Clock.System.now()
    val currentInstant = this.toInstant(timeZone)
    val stringBuilder = StringBuilder()
    if (now - 5.minutes < currentInstant) {
        stringBuilder.append(context.getString(R.string.date_time_just_now_message))
    } else if (now - 60.minutes < currentInstant) {
        val diff = (now - currentInstant).inWholeMinutes.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.minutes_presentation,
                diff,
                diff
            )
        )
    } else if (now - 24.hours < currentInstant) {
        val diff = (now - currentInstant).inWholeHours.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.hours_presentation,
                diff,
                diff
            )
        )
    } else if (now - 30.days < currentInstant) {
        val diff = (now - currentInstant).inWholeDays.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.days_presentation,
                diff,
                diff
            )
        )
    } else if (now - 30.days * 12 < currentInstant) {
        val diff = ((now - currentInstant).inWholeDays / 30).toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.months_presentation,
                diff,
                diff
            )
        )
    } else {
        val diff = ((now - currentInstant).inWholeDays / (30 * 12)).toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.years_presentation,
                diff,
                diff
            )
        )
    }
    if (now - 5.minutes >= currentInstant) {
        stringBuilder.append(" ").append(context.getString(R.string.date_time_ago_message))
    }
    return stringBuilder.toString()
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun rememberIsSmallWindow(): Boolean {
    val activity = LocalActivity.current!!
    val windowSizeClass = calculateWindowSizeClass(activity)
    val configuration = LocalConfiguration.current

    return remember(windowSizeClass, configuration) {
        // Check if either dimension is Compact (handles multi-window/split-screen)
        val hasCompactDimension = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
                windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

        // Check physical screen characteristics
        val isPhysicallySmall = configuration.smallestScreenWidthDp < 600 ||
                configuration.screenWidthDp < 600 ||
                configuration.screenHeightDp < 600

        // Special handling for foldables
        val isFolded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(activity)
            val bounds = metrics.bounds
            val density = activity.resources.displayMetrics.density
            bounds.width() / density < 600 || bounds.height() / density < 600
        } else true

        hasCompactDimension && (isPhysicallySmall || isFolded)
    }
}
