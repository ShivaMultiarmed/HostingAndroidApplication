package mikhail.shell.video.hosting.presentation.video.screen

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Liking.DISLIKED
import mikhail.shell.video.hosting.domain.models.Liking.LIKED
import mikhail.shell.video.hosting.domain.models.Liking.NONE
import mikhail.shell.video.hosting.domain.models.Subscription.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.Subscription.SUBSCRIBED
import mikhail.shell.video.hosting.presentation.comments.models.CommentUi
import mikhail.shell.video.hosting.presentation.player.LocalMiniPlayerDimensionsState
import mikhail.shell.video.hosting.presentation.player.LocalPlayerState
import mikhail.shell.video.hosting.presentation.player.MiniPlayerDimensions
import mikhail.shell.video.hosting.presentation.player.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.Description
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.EditButton
import mikhail.shell.video.hosting.presentation.utils.EmptyComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.MoreButton
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.PrimaryToggleButton
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.format
import mikhail.shell.video.hosting.presentation.utils.rememberIsSmallWindow
import mikhail.shell.video.hosting.presentation.utils.rememberPageableBoxState
import mikhail.shell.video.hosting.presentation.utils.toRoundString
import mikhail.shell.video.hosting.presentation.utils.toSubscribers
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.ui.theme.Black

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMotionApi::class)
@Composable
fun VideoScreen(
    state: VideoScreenState,
    playerProvider: () -> Player,
    onAction: (VideoScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val windowSize = LocalWindowInfo.current.containerDpSize
    val activity = LocalActivity.current!!
    val density = LocalDensity.current
    var playerState by LocalPlayerState.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bottomSheetHeight by remember {
        mutableStateOf(0.dp)
    }
    val imeInset = with(density) {
        WindowInsets.ime.getBottom(this).toDp()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(
                    bottom = when {
                        sheetState.isVisible -> bottomSheetHeight
                        else -> 0.dp
                    }
                ),
                hostState = snackBarHostState
            )
        }
    ) { padding ->
        var miniPlayerDimensions by LocalMiniPlayerDimensionsState.current
        var isFullScreen by retain {
            mutableStateOf(false)
        }
        var ratio by rememberSaveable {
            mutableFloatStateOf(16f / 9)
        }
        val safeRatio = if (!ratio.isNaN()) ratio else 16f / 9
        val direction = LocalLayoutDirection.current
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenContentWidth =
                maxWidth - padding.calculateLeftPadding(direction) - padding.calculateRightPadding(
                    direction
                )
            val screenContentHeight =
                maxHeight - padding.calculateTopPadding() - padding.calculateBottomPadding()

            val idlePlayerWidth =
                if (screenContentWidth > 0.dp) screenContentWidth else windowSize.width
            val idlePlayerHeight =
                idlePlayerWidth / (if (safeRatio >= 1f) safeRatio else 16f / 9)

            var playerContainerWidth by remember {
                mutableStateOf(0.dp)
            }
            var playerContainerHeight by remember {
                mutableStateOf(0.dp)
            }

            val miniPlayerMaxDimension = min(screenContentWidth, screenContentHeight) * 0.5f
            val exitPlayerWidth =
                if (safeRatio >= 1f) miniPlayerMaxDimension else safeRatio * miniPlayerMaxDimension
            val exitPlayerHeight =
                if (safeRatio >= 1f) miniPlayerMaxDimension / safeRatio else miniPlayerMaxDimension

            val exitThresholdHeight = 0.4f * screenContentHeight

            val playerOffset = retain {
                Animatable(
                    DpOffset(
                        x = miniPlayerDimensions?.x?.dp ?: 0.dp,
                        y = miniPlayerDimensions?.y?.dp ?: 0.dp
                    ),
                    DpOffset.VectorConverter
                )
            }


            fun progressAt(y: Dp) = if (exitThresholdHeight > 0.dp) (y / exitThresholdHeight).coerceIn(0f..1f) else 0f
            fun playerWidthAt(y: Dp) =
                exitPlayerWidth + (1 - progressAt(y)) * (idlePlayerWidth - exitPlayerWidth)

            fun playerHeightAt(y: Dp) =
                exitPlayerHeight + (1 - progressAt(y)) * (idlePlayerHeight - exitPlayerHeight)

            val exitProgress = progressAt(playerOffset.value.y)
            val currentWidth = playerWidthAt(playerOffset.value.y)
            val currentHeight = playerHeightAt(playerOffset.value.y)

            suspend fun settle(minimize: Boolean, toLeft: Boolean = true) {
                playerOffset.animateTo(
                    targetValue = DpOffset(
                        x = if (minimize && !toLeft) screenContentWidth - exitPlayerWidth else 0.dp,
                        y = if (minimize) screenContentHeight - exitPlayerHeight else 0.dp
                    )
                )
            }

            suspend fun exit(toLeft: Boolean = true) {
                settle(
                    minimize = true,
                    toLeft = toLeft
                )
                miniPlayerDimensions = MiniPlayerDimensions(
                    x = playerOffset.value.x.value.toInt(),
                    y = playerOffset.value.y.value.toInt(),
                    aspectRatio = safeRatio
                )
                onAction(VideoScreenAction.Exit)
            }

            LaunchedEffect(Unit) {
                settle(minimize = false)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(
                            alpha = 1 - exitProgress.coerceWithBreakPoint(
                                0f..1f,
                                0.4f
                            )
                        )
                    )
                    .padding(padding),
            ) {
                val scrollState = rememberScrollState()
                val orientation = LocalConfiguration.current.orientation
                val isSmallWindow = rememberIsSmallWindow()
                val targetOrientation = remember(isFullScreen, isSmallWindow) {
                    if (isSmallWindow) {
                        if (isFullScreen && safeRatio >= 1f) {
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
                BackHandler(true) {
                    if (isFullScreenReached) {
                        isFullScreen = false
                    } else {
                        coroutineScope.launch {
                            exit()
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .then(
                            when (isFullScreenReached) {
                                true -> Modifier.fillMaxSize()
                                false -> Modifier
                                    .padding(
                                        top = playerOffset.value.y,
                                        start = playerOffset.value.x
                                    )
                                    .then(
                                        when {
                                            safeRatio.isNaN() ->
                                                Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(16f / 9)

                                            else -> Modifier.size(
                                                width = currentWidth,
                                                height = currentHeight
                                            )
                                        }
                                    )
                                    .clip(RoundedCornerShape(exitProgress * 10.dp))
                            }
                        )
                        .background(
                            Color.Black.copy(
                                alpha = 1 - exitProgress.coerceWithBreakPoint(
                                    0f..1f,
                                    0.4f
                                )
                            )
                        )
                        .then(
                            when (isFullScreenReached) {
                                true -> Modifier
                                false -> Modifier.pointerInput(
                                    screenContentWidth,
                                    screenContentHeight,
                                    exitPlayerWidth,
                                    exitPlayerHeight
                                ) {
                                    var targetOffset = playerOffset.value
                                    val onRelease: () -> Unit = {
                                        val minimize = targetOffset.y >= exitThresholdHeight
                                        val screenCenter = screenContentWidth / 2
                                        val playerCenter = playerWidthAt(targetOffset.y) / 2
                                        val toLeft = targetOffset.x + playerCenter <= screenCenter
                                        coroutineScope.launch {
                                            if (minimize) {
                                                exit(toLeft)
                                            } else {
                                                settle(minimize, toLeft)
                                            }
                                        }
                                    }
                                    detectDragGestures(
                                        onDragStart = {
                                            targetOffset = playerOffset.value
                                        },
                                        onDragEnd = onRelease,
                                        onDragCancel = onRelease
                                    ) { change, gragAmount ->
                                        if (targetOffset.y == 0.dp && gragAmount.y.toDp() <= 0.dp) {
                                            return@detectDragGestures
                                        }
                                        val nextTargetOffset = targetOffset.copy(
                                            x = (targetOffset.x + gragAmount.x.toDp()).coerceIn(
                                                0.dp..(screenContentWidth - playerWidthAt(
                                                    targetOffset.y
                                                )).coerceAtLeast(0.dp)
                                            ),
                                            y = (targetOffset.y + gragAmount.y.toDp()).coerceIn(
                                                0.dp..(screenContentHeight - playerHeightAt(
                                                    targetOffset.y
                                                )).coerceAtLeast(0.dp)
                                            )
                                        )
                                        targetOffset = nextTargetOffset
                                        coroutineScope.launch {
                                            playerOffset.snapTo(targetValue = nextTargetOffset)
                                        }
                                        change.consume()
                                    }
                                }
                            }
                        )
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                playerContainerWidth = coordinates.size.width.toDp()
                                playerContainerHeight = coordinates.size.height.toDp()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    PlayerComponent(
                        modifier = Modifier.matchParentSize(),
                        playerProvider = playerProvider,
                        onRatioObtained = {
                            ratio = it
                        },
                        isFullScreen = isFullScreen,
                        onFullscreen = {
                            isFullScreen = it
                        },
                        controlsImage = !playerState.inPipMode
                    )
                }
                RetainedEffect(isFullScreenReached) {
                    playerState = playerState.copy(fullScreen = isFullScreenReached)
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
                    onRetire {}
                }
                RetainedEffect(targetOrientation) {
                    activity.requestedOrientation = targetOrientation
                    onRetire {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
                if (state.video != null) {
                    if (!isFullScreenReached) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(1 - exitProgress.coerceWithBreakPoint(0f..1f, 0.4f))
                                .background(Black)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp
                                    )
                                )
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp)
                                .onGloballyPositioned { coordinates ->
                                    val newHeight = with(density) {
                                        coordinates.size.height.toDp()
                                    }
                                    if (bottomSheetHeight != newHeight) {
                                        bottomSheetHeight = newHeight
                                    }
                                }
                                .verticalScroll(scrollState)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.video.videoTitle,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 20.sp,
                                    maxLines = 2,
                                    lineHeight = 22.sp
                                )
                            }
                            var isDescriptionVisible by rememberSaveable {
                                mutableStateOf(false)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
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
                                        text = state.video.dateTime.format(context),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        5.dp,
                                        Alignment.End
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (state.userId == state.video.ownerId) {
                                        var isDeletingDialogOpen by rememberSaveable {
                                            mutableStateOf(false)
                                        }
                                        var isAdvancedDialogOpen by rememberSaveable {
                                            mutableStateOf(false)
                                        }
                                        Box {
                                            EditButton(
                                                modifier = Modifier.size(24.dp),
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
                                                isLoading = state.isRemoving,
                                                onDismiss = {
                                                    isDeletingDialogOpen = false
                                                },
                                                dialogTitle = stringResource(R.string.video_delete_warning_title),
                                                dialogDescription = stringResource(R.string.video_delete_warning_message)
                                            )
                                        }
                                    }
                                    if (state.video.videoDescription != null) {
                                        MoreButton(
                                            isVisible = isDescriptionVisible,
                                            onClick = {
                                                isDescriptionVisible = !isDescriptionVisible
                                            }
                                        )
                                    }
                                }
                            }
                            if (state.video.videoDescription != null) {
                                Description(
                                    modifier = Modifier.fillMaxWidth(),
                                    isVisible = isDescriptionVisible,
                                    description = state.video.videoDescription
                                )
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
                            ConstraintLayout(
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
                                    .padding(vertical = 6.dp, horizontal = 10.dp),
                            ) {
                                val (label, button) = createRefs()
                                Text(
                                    modifier = Modifier.constrainAs(label) {
                                        start.linkTo(parent.start)
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                    },
                                    text = stringResource(R.string.comments_title),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                val buttonBg = MaterialTheme.colorScheme.tertiaryContainer.let {
                                    it.copy(
                                        red = it.red - 20f / 255,
                                        green = it.green - 20f / 255,
                                        blue = it.blue - 20f / 255
                                    )
                                }
                                IconButton(
                                    modifier = Modifier.constrainAs(button) {
                                        end.linkTo(parent.end)
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                    },
                                    onClick = {
                                        coroutineScope.launch {
                                            sheetState.show()
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = buttonBg,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Default.ModeComment,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        if (sheetState.isVisible) {
                            CommentsBottomSheet(
                                modifier = Modifier.height(bottomSheetHeight - BottomSheetDefaults.SheetPeekHeight - imeInset),
                                userId = state.userId,
                                sheetState = sheetState,
                                commentsState = state.commentsState,
                                onAction = onAction
                            )
                        }
                        LaunchedEffect(sheetState.isVisible) {
                            if (sheetState.isVisible && state.commentsState.hasMore) {
                                onAction(VideoScreenAction.LoadNextCommentsPart)
                            }
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
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    modifier: Modifier = Modifier,
    userId: Long,
    sheetState: SheetState,
    commentsState: CommentsState,
    onAction: (VideoScreenAction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        sheetState = sheetState,
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
            }
        },
        shape = RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp
        ),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (commentsState.comments != null) {
                    val pageableBoxState = rememberPageableBoxState(
                        items = commentsState.comments,
                        hasMore = commentsState.hasMore,
                        error = commentsState.error,
                        isLoading = commentsState.isLoading,
                    )
                    PageableBox(
                        modifier = Modifier.matchParentSize(),
                        state = pageableBoxState,
                        itemComponent = {
                            CommentBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                owns = it.userId == userId,
                                onAction = onAction,
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
                        onReachedEnd = {
                            onAction(VideoScreenAction.LoadNextCommentsPart)
                        },
                        onReload = {
                            onAction(VideoScreenAction.LoadNextCommentsPart)
                        }
                    )
                } else if (commentsState.isStarting) {
                    StartingComponent(
                        modifier = Modifier.matchParentSize()
                    )
                } else if (commentsState.error != null) {
                    ErrorComponent(
                        modifier = Modifier.matchParentSize(),
                        onRetry = {
                            onAction(VideoScreenAction.RestartComments)
                        }
                    )
                }
            }
            CommentForm(
                text = commentsState.comment.text.value,
                error = commentsState.comment.text.error,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun CommentBox(
    modifier: Modifier = Modifier,
    owns: Boolean,
    comment: CommentUi,
    onAction: (VideoScreenAction) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        var isMenuVisible by rememberSaveable {
            mutableStateOf(false)
        }
        ContextMenu(
            isExpanded = isMenuVisible,
            menuItems = listOf(
                MenuItem(
                    title = stringResource(R.string.comment_edit_button),
                    onClick = {
                        onAction(
                            VideoScreenAction.EditComment(comment.commentId)
                        )
                        isMenuVisible = false
                    }
                ),
                MenuItem(
                    title = stringResource(R.string.comment_delete_button),
                    onClick = {
                        onAction(VideoScreenAction.RemoveComment(comment.commentId))
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
                        onAction(VideoScreenAction.OpenProfile(comment.userId))
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
                        text = comment.nick + " - " + comment.dateTime.format(context),
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
    error: TextError?,
    onAction: (VideoScreenAction) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        val (textField, button) = createRefs()
        InputField(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .heightIn(max = 300.dp)
                .border(
                    width = 1.dp,
                    color = when (error != null) {
                        true -> MaterialTheme.colorScheme.error
                        false -> Color.Transparent
                    },
                    shape = RoundedCornerShape(5.dp)
                )
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .constrainAs(textField) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(button.start, 10.dp)
                    width = Dimension.fillToConstraints
                },
            value = text,
            maxLines = 20,
            onValueChange = {
                onAction(VideoScreenAction.ChangeCommentText(it))
            },
            label = stringResource(R.string.comments_leave_hint)
        )
        PrimaryProgressButton(
            modifier = Modifier.constrainAs(button) {
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
            enabled = text.isNotEmpty(),
            onClick = {
                onAction(VideoScreenAction.SubmitComment)
            },
            icon = Icons.AutoMirrored.Rounded.Send
        )
    }
}

private fun Float.coerceWithBreakPoint(
    range: ClosedRange<Float>,
    breakPoint: Float
): Float {
    require(breakPoint in range) {
        "Breakpoint $breakPoint is out of range $range"
    }
    val coerced = coerceIn(range)
    return when {
        coerced < breakPoint -> 0f
        breakPoint == range.endInclusive -> 1f
        else -> (coerced - breakPoint) / (range.endInclusive - breakPoint)
    }
}