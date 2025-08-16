package mikhail.shell.video.hosting.presentation.video.screen

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.runtime.derivedStateOf
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.comment.CommentError
import mikhail.shell.video.hosting.domain.models.Action
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Liking.DISLIKED
import mikhail.shell.video.hosting.domain.models.Liking.LIKED
import mikhail.shell.video.hosting.domain.models.Liking.NONE
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.models.Subscription.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.Subscription.SUBSCRIBED
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.presentation.exoplayer.LocalPlayerState
import mikhail.shell.video.hosting.presentation.exoplayer.PlayerComponent
import mikhail.shell.video.hosting.presentation.models.CommentUi
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.EditButton
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.PrimaryToggleButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.reachedBottom
import mikhail.shell.video.hosting.presentation.utils.toRoundString
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.ui.theme.Black
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    userId: Long,
    state: VideoScreenState,
    onRefresh: () -> Unit,
    onRate: (Liking) -> Unit,
    onSubscribe: (Subscription) -> Unit,
    player: Player,
    onChannelLinkClick: (Long) -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Long) -> Unit,
    onComment: (commentId: Long?, text: String) -> Unit = { _, _ -> },
    onRemoveComment: (commentId: Long) -> Unit = {},
    onLoadComments: (before: LocalDateTime) -> Unit = {},
    onObserve: () -> Unit = {},
    onUnobserve: () -> Unit = {},
    onGoToProfile: (userId: Long) -> Unit = {},
    onShare: (Long) -> Unit = {},
    onVideoNotFound: () -> Unit = {},
    onAuthenticationRequired: () -> Unit = {}
) {
    val activity = LocalActivity.current!!
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScreenActive by rememberSaveable { mutableStateOf(false) }
    val playerState = LocalPlayerState.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        if (state.videoDetails != null) {
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
                            isScreenActive = false
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        } else if (event == Lifecycle.Event.ON_START) {
                            isScreenActive = true
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
                                text = state.videoDetails.videoTitle,
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
                                text = state.videoDetails.videoTitle,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.videoDetails.dateTime.toPresentation(context),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                            if (state.videoDetails.ownerId == userId) {
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
                                                        onUpdate(state.videoDetails.videoId)
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
                                        onSubmit = onDelete,
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
                                    .clickable { onChannelLinkClick(state.videoDetails.channelId) }
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = state.videoDetails.avatarUrl,
                                    contentDescription = stringResource(R.string.channel_link),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                )
                                Text(
                                    text = state.videoDetails.videoTitle,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 13.dp),
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = state.videoDetails.views.toViews(),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(end = 5.dp)
                            )
                            PrimaryToggleButton(
                                toggled = state.videoDetails.subscription == SUBSCRIBED,
                                onClick = {
                                    val subscriptionState = when (state.videoDetails.subscription) {
                                        SUBSCRIBED -> NOT_SUBSCRIBED
                                        else -> SUBSCRIBED
                                    }
                                    onSubscribe(subscriptionState)
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
                            val likeVector = when (state.videoDetails.liking) {
                                LIKED -> Icons.Rounded.ThumbUp
                                else -> Icons.Outlined.ThumbUp
                            }
                            ActionButton(
                                icon = likeVector,
                                text = state.videoDetails.likes.toRoundString(), // TODO rounded to 2 digits after a point and with suffix
                                onClick = {
                                    onRate(if (state.videoDetails.liking != LIKED) LIKED else NONE)
                                }
                            )
                            val dislikeVector = when (state.videoDetails.liking) {
                                DISLIKED -> Icons.Rounded.ThumbDown
                                else -> Icons.Outlined.ThumbDown
                            }
                            ActionButton(
                                icon = dislikeVector,
                                text = state.videoDetails.dislikes.toRoundString(), // TODO rounded to 2 digits after a point and with suffix
                                onClick = {
                                    onRate(if (state.videoDetails.liking != DISLIKED) DISLIKED else NONE)
                                }
                            )
                            ActionButton(
                                icon = Icons.Rounded.Share,
                                text = stringResource(R.string.video_share),
                                onClick = {
                                    onShare(state.videoDetails.videoId)
                                }
                            )
                            ActionButton(
                                icon = Icons.Outlined.Download,
                                text = stringResource(R.string.video_download_button),
                                onClick = {
                                    Intent(context, VideoDownloadingService::class.java).also {
                                        it.action =
                                            "mikhail.shell.video.hosting.ACTION_LAUNCH_DOWNLOADING"
                                        it.putExtra("videoId", state.videoDetails.videoId)
                                        context.startService(it)
                                    }
                                }
                            )
                        }
                        var commentsVisible by rememberSaveable { mutableStateOf(false) }
                        val sheetState = rememberModalBottomSheetState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .clickable {
                                    coroutineScope
                                        .launch {
                                            sheetState.show()
                                        }
                                        .invokeOnCompletion {
                                            if (sheetState.isVisible) {
                                                commentsVisible = true
                                            }
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
                            if (commentsVisible) {
                                LaunchedEffect(Unit) {
                                    val dateTimeThreshold = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    onLoadComments(dateTimeThreshold)
                                }
                                if (state.comments != null) {
                                    CommentsBottomSheet(
                                        state = sheetState,
                                        onDismiss = {
                                            commentsVisible = false
                                        },
                                        userId = userId,
                                        comments = state.comments,
                                        onSubmit = onComment,
                                        onRemoveComment = onRemoveComment,
                                        onObserve = onObserve,
                                        onUnobserve = onUnobserve,
                                        onLoad = onLoadComments,
                                        commentError = state.commentError,
                                        actionComment = state.actionComment,
                                        onGoToProfile = onGoToProfile
                                    )
                                }
                            }

                        }
                    }
                }
            }

        } else if (state.isLoading) {
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
    StandardComplexErrorHandler(
        error = state.loadingError,
        snackBarHostState = snackBarHostState,
        notFoundMessage = stringResource(R.string.video_not_found),
        notFoundHandler = onVideoNotFound,
        authenticationRequiredHandler = onAuthenticationRequired
    )
    StandardComplexErrorHandler(
        error = state.likingError,
        snackBarHostState = snackBarHostState,
        notFoundMessage = stringResource(R.string.video_not_found),
        notFoundHandler = onVideoNotFound,
        authenticationRequiredHandler = onAuthenticationRequired
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    userId: Long,
    state: SheetState,
    comments: List<CommentUi>,
    commentError: Error? = null,
    actionComment: ActionModel<CommentUi>? = null,
    onSubmit: (commentId: Long?, text: String) -> Unit = { _, _ -> },
    onRemoveComment: (commentId: Long) -> Unit = {},
    onDismiss: () -> Unit = {},
    onObserve: () -> Unit = {},
    onUnobserve: () -> Unit = {},
    onLoad: (LocalDateTime) -> Unit = {},
    onGoToProfile: (userId: Long) -> Unit = {},
    onVideoNotFound: () -> Unit = {},
    onAuthenticationRequired: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(10.dp),
        ) {
            var initialCommentUi by remember { mutableStateOf(null as CommentUi?) }
            if (comments.isNotEmpty()) {
                val lazyListState = rememberLazyListState()
                val reachedBottom by remember { derivedStateOf { lazyListState.reachedBottom(4) } }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = lazyListState
                ) {
                    items(comments) { comment ->
                        CommentBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            own = comment.userId == userId,
                            comment = comment,
                            onEdit = { _, _ ->
                                initialCommentUi = comment
                            },
                            onRemove = onRemoveComment,
                            onGoToProfile = onGoToProfile
                        )
                    }
                }
                LaunchedEffect(comments) {
                    initialCommentUi = null
                }
                LaunchedEffect(reachedBottom) {
                    if (reachedBottom) {
                        val earliestCommentDateTime = comments.lastOrNull()?.dateTime ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        onLoad(earliestCommentDateTime)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.comments_empty_message)
                    )
                }
            }
            val snackBarHostState = remember { SnackbarHostState() }
            SnackbarHost(snackBarHostState)
            LaunchedEffect(commentError) {
                commentError?.let {
                    val message = when (it) {
                        CommentError.TEXT_TOO_LARGE -> context.getString(
                            R.string.text_too_large_error,
                            ValidationRules.MAX_TEXT_LENGTH
                        )

                        CommentError.TEXT_EMPTY -> context.getString(R.string.text_empty_error)
                        else -> null
                    }
                    message?.let {
                        snackBarHostState.showSnackbar(
                            message = it,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
            LaunchedEffect(actionComment) {
                actionComment?.let {
                    val message = when (it.action) {
                        Action.ADD -> context.getString(R.string.comment_add_success)
                        Action.REMOVE -> context.getString(R.string.comment_delete_success)
                        Action.UPDATE -> context.getString(R.string.comment_edit_success)
                    }
                    snackBarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            CommentForm(
                initialCommentUi = initialCommentUi,
                onSubmit = onSubmit,
                commentError = commentError,
                actionComment = actionComment
            )
        }
        SnackbarHost(hostState = snackBarHostState)
    }

    DisposableEffect(Unit) {
        onObserve()
        onDispose {
            onUnobserve()
        }
    }

    StandardComplexErrorHandler(
        error = commentError,
        snackBarHostState = snackBarHostState,
        notFoundMessage = stringResource(R.string.video_not_found),
        notFoundHandler = onVideoNotFound,
        authenticationRequiredHandler = onAuthenticationRequired
    )
}

@Composable
fun CommentBox(
    modifier: Modifier = Modifier,
    own: Boolean = false,
    comment: CommentUi,
    onEdit: (commentId: Long, text: String) -> Unit = { _, _ -> },
    onRemove: (commentId: Long) -> Unit = {},
    onGoToProfile: (userId: Long) -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        var isMenuVisible by rememberSaveable { mutableStateOf(false) }
        if (isMenuVisible) {
            ContextMenu(
                isExpanded = isMenuVisible,
                menuItems = listOf(
                    MenuItem(
                        title = stringResource(R.string.comment_edit_button),
                        onClick = {
                            onEdit(comment.commentId, comment.text)
                            isMenuVisible = false
                        }
                    ),
                    MenuItem(
                        title = stringResource(R.string.comment_delete_button),
                        onClick = {
                            onRemove(comment.commentId)
                            isMenuVisible = false
                        }
                    )
                ),
                onDismiss = {
                    isMenuVisible = false
                }
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .clickable {
                        onGoToProfile(comment.userId)
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
                    if (own) {
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
fun CommentForm(
    initialCommentUi: CommentUi? = null,
    onSubmit: (commentId: Long?, text: String) -> Unit = { _, _ -> },
    actionComment: ActionModel<CommentUi>? = null,
    commentError: Error? = null
) {
    var text by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(initialCommentUi) {
        initialCommentUi?.let { text = it.text }
    }
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
                    color = when (commentError) {
                        null -> Color.Transparent
                        else -> MaterialTheme.colorScheme.error
                    },
                    shape = RoundedCornerShape(5.dp)
                )
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .weight(1f),
            value = text,
            maxLines = 100,
            onValueChange = {
                text = it
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
                onSubmit(initialCommentUi?.commentId, text)
            },
            icon = Icons.AutoMirrored.Rounded.Send
        )
        LaunchedEffect(actionComment) {
            if (actionComment?.action != Action.REMOVE) {
                text = ""
            }
        }
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
        val diff = (currentInstant - now).inWholeMinutes.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.minutes_presentation,
                diff,
                diff
            )
        )
    } else if (now - 24.hours < currentInstant) {
        val diff = (currentInstant - now).inWholeHours.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.hours_presentation,
                diff,
                diff
            )
        )
    } else if (now - 30.days < currentInstant) {
        val diff = (currentInstant - now).inWholeDays.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.days_presentation,
                diff,
                diff
            )
        )
    } else if (now - 30.days * 12 < currentInstant) {
        val diff = ((currentInstant - now).inWholeDays / 30).toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.months_presentation,
                diff,
                diff
            )
        )
    } else {
        val diff = ((currentInstant - now).inWholeDays / (30 * 12)).toInt()
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
