@file:kotlin.OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package mikhail.shell.video.hosting.presentation.player

import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mikhail.shell.video.hosting.R
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.pow

@Serializable
data class PlayerState(
    val prepared: Boolean = false,
    val hidden: Boolean = true,
    val fullScreen: Boolean = false
)

val PlayerStateSaver = object : Saver<MutableState<PlayerState>, String> {
    override fun restore(value: String): MutableState<PlayerState> {
        return mutableStateOf(
            Json.decodeFromString(
                deserializer = PlayerState.serializer(),
                string = value
            )
        )
    }

    override fun SaverScope.save(value: MutableState<PlayerState>): String {
        return Json.encodeToString(
            serializer = PlayerState.serializer(),
            value = value.value
        )
    }
}

val LocalPlayerState = compositionLocalOf {
    mutableStateOf(PlayerState())
}

@Serializable
data class MiniPlayerPosition(
    val x: Int = 0,
    val y: Int = 0
)

val LocalMiniPlayerPositionState = compositionLocalOf {
    mutableStateOf(MiniPlayerPosition())
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    playerProvider: () -> Player,
    onFullscreen: (Boolean) -> Unit,
    isFullScreen: Boolean = false,
    onRatioObtained: ((Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val player = retain {
        playerProvider()
    }
    var playerState by rememberSaveable { mutableIntStateOf(player.playbackState) }
    var isPlaying by rememberSaveable { mutableStateOf(player.isPlaying) }
    var duration by rememberSaveable { mutableLongStateOf(player.duration) }
    var position by rememberSaveable { mutableLongStateOf(player.currentPosition) }
    var savedPlayState by rememberSaveable { mutableStateOf(player.isPlaying) }
    var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        VideoSurface(
            modifier = Modifier.matchParentSize(),
            playerProvider = playerProvider
        )
        val seekRange = 5000L
        PlayerControls(
            modifier = Modifier.matchParentSize(),
            position = position,
            duration = duration,
            isPlaying = isPlaying,
            onPlay = player::play,
            onPause = player::pause,
            onSeekBack = {
                val newPosition = (player.currentPosition - seekRange).coerceAtLeast(0)
                player.seekTo(newPosition)
            },
            onSeekForward = {
                val newPosition =
                    (player.currentPosition + seekRange).coerceAtMost(player.duration - 1)
                player.seekTo(newPosition)
            },
            onSeek = player::seekTo,
            isFullScreen = isFullScreen,
            onFullscreen = onFullscreen
        )
    }
    RetainedEffect(Unit) {
        val playerListener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {

                aspectRatio = videoSize.width.toFloat() / videoSize.height
                onRatioObtained?.invoke(aspectRatio)
            }

            override fun onIsPlayingChanged(newIsPlaying: Boolean) {
                if (playerState != Player.STATE_BUFFERING) {
                    isPlaying = newIsPlaying
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playerState = playbackState
                if (playbackState == Player.STATE_READY && duration < 0L) {
                    duration = player.duration
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                position = newPosition.positionMs
            }
        }

        player.addListener(playerListener)

        onRetire {
            player.removeListener(playerListener)
        }
    }
    LaunchedEffect(isPlaying) {
        val changePeriod = 250L
        while (isActive && isPlaying) {
            position = player.currentPosition
            val delayDuration = changePeriod - (position % changePeriod)
            delay(delayDuration)
        }
    }
    RetainedEffect(Unit) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        val audioListener = AudioManager.OnAudioFocusChangeListener {
            if (it == AUDIOFOCUS_LOSS_TRANSIENT) {
                if (player.isPlaying) {
                    savedPlayState = true
                    player.pause()
                } else {
                    savedPlayState = false
                }
            } else if (savedPlayState && it in arrayOf(
                    AUDIOFOCUS_GAIN,
                    AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
                    AUDIOFOCUS_GAIN_TRANSIENT,
                    AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
                )
            ) {
                player.play()
            }
        }
        val audioFocusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioListener).build()
        audioManager.requestAudioFocus(audioFocusRequest)

        onRetire {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
    }
    BackHandler(enabled = isFullScreen) {
        onFullscreen?.invoke(false)
    }
}

@Composable
internal fun VideoSurface(
    modifier: Modifier = Modifier,
    playerProvider: () -> Player
) {
    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                useController = false
                this.player = playerProvider()
            }
        }
    )
}

@Composable
internal fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    isFullScreen: Boolean = false,
    onFullscreen: (Boolean) -> Unit,
) {
    val windowSize = LocalWindowInfo.current.containerDpSize
    val windowSizeClass = remember {
        WindowSizeClass.calculateFromSize(
            DpSize(windowSize.width, windowSize.height)
        )
    }
    val coroutineScope = rememberCoroutineScope()
    var isInteracting by rememberSaveable {
        mutableStateOf(false)
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val controlsShowDuration = 3000
    var notActiveTimer by rememberSaveable {
        mutableIntStateOf(0)
    }
    var controlsAlpha by rememberSaveable {
        mutableFloatStateOf(0f)
    }
    val animatedControlsAlpha by animateFloatAsState(
        targetValue = controlsAlpha,
        animationSpec = tween(300)
    )
    LaunchedEffect(notActiveTimer) {
        when (notActiveTimer) {
            controlsShowDuration -> controlsAlpha = 1f
            0 -> controlsAlpha = 0f
        }
    }
    LaunchedEffect(isInteracting) {
        if (isInteracting) {
            notActiveTimer = controlsShowDuration
        } else if (notActiveTimer > 0) {
            delay(controlsShowDuration.toLong())
            notActiveTimer = 0
        }
    }
    ConstraintLayout(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {}
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull() ?: continue
                        when {
                            !change.previousPressed && change.pressed -> { // Pointer down
                                isInteracting = true
                            }

                            change.pressed && change.positionChange() != Offset.Zero -> { // Dragging
                                isInteracting = true
                            }

                            change.previousPressed && !change.pressed -> { // Pointer up or Canceled
                                isInteracting = false
                            }
                        }
                    }
                }
            }
    ) {
        val (seekBack, seekForward, playBtn, toolBar) = createRefs()

        var seekBackAlpha by rememberSaveable { mutableFloatStateOf(0f) }
        val animatedSeekBackAlpha by animateFloatAsState(
            targetValue = seekBackAlpha,
            animationSpec = tween(durationMillis = 300)
        )
        Box(
            modifier = Modifier
                .graphicsLayer(alpha = animatedSeekBackAlpha)
                .fillMaxHeight()
                .fillMaxWidth(0.35f)
                .constrainAs(seekBack) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .clip(createStadiumShape(Direction.Ltr))
                .then(
                    when {
                        animatedSeekBackAlpha > 0 -> Modifier.shimmer(
                            direction = Direction.Rtl,
                            baseColor = Color.White.copy(alpha = 0.15f),
                            accentColor = Color.White.copy(alpha = 0.3f)
                        )

                        else -> Modifier
                    }
                )
                .combinedClickable(
                    onClick = {
                        isInteracting = true
                        isInteracting = false
                    },
                    onDoubleClick = {
                        coroutineScope.launch {
                            seekBackAlpha = 1f
                            onSeekBack()
                            delay(300)
                            seekBackAlpha = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                tint = Color(0f, 0f, 0f, 0.6f),
                imageVector = Icons.Rounded.FastRewind,
                contentDescription = stringResource(R.string.player_seek_backward_hint)
            )
        }
        var seekForwardAlpha by rememberSaveable { mutableFloatStateOf(0f) }
        val animatedSeekForwardAlpha by animateFloatAsState(
            targetValue = seekForwardAlpha,
            animationSpec = tween(durationMillis = 300)
        )
        Box(
            modifier = Modifier
                .graphicsLayer(alpha = animatedSeekForwardAlpha)
                .fillMaxHeight()
                .fillMaxWidth(0.35f)
                .constrainAs(seekForward) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .clip(createStadiumShape(Direction.Rtl))
                .then(
                    when {
                        animatedSeekForwardAlpha > 0 -> Modifier.shimmer(
                            direction = Direction.Ltr,
                            baseColor = Color.White.copy(alpha = 0.15f),
                            accentColor = Color.White.copy(alpha = 0.3f)
                        )
                        else -> Modifier
                    }
                )
                .combinedClickable(
                    onClick = {
                        isInteracting = true
                        isInteracting = false
                    },
                    onDoubleClick = {
                        coroutineScope.launch {
                            seekForwardAlpha = 1f
                            onSeekForward()
                            delay(300)
                            seekForwardAlpha = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                tint = Color.Black.copy(alpha = 0.6f),
                imageVector = Icons.Rounded.FastForward,
                contentDescription = stringResource(R.string.player_seek_forward_hint)
            )
        }
        if (animatedControlsAlpha > 0) {
            Button(
                modifier = Modifier
                    .size(
                        when (windowSizeClass.widthSizeClass) {
                            WindowWidthSizeClass.Compact -> 40.dp
                            WindowWidthSizeClass.Medium -> 50.dp
                            else -> 60.dp
                        }
                    )
                    .clip(CircleShape)
                    .graphicsLayer(alpha = animatedControlsAlpha)
                    .constrainAs(playBtn) {
                        centerTo(parent)
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(4.dp),
                onClick = {
                    if (isPlaying) {
                        onPause()
                    } else {
                        onPlay()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    imageVector = when (isPlaying) {
                        true -> Icons.Rounded.Pause
                        false -> Icons.Rounded.PlayArrow
                    },
                    tint = Color.Black.copy(alpha = 0.8f),
                    contentDescription = stringResource(R.string.player_main_button_hint)
                )
            }
            if (duration >= 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(alpha = animatedControlsAlpha)
                        .constrainAs(toolBar) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = when (windowSizeClass.widthSizeClass) {
                                    WindowWidthSizeClass.Compact -> 15.dp
                                    WindowWidthSizeClass.Medium -> 17.dp
                                    else -> 20.dp
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val durationString = duration.formatDuration()
                        val positionString = position.formatDuration()
                        Text(
                            text = "$positionString / $durationString",
                            color = Color.White,
                            fontSize = when (windowSizeClass.widthSizeClass) {
                                WindowWidthSizeClass.Compact -> 11.sp
                                WindowWidthSizeClass.Medium -> 14.sp
                                else -> 16.sp
                            }
                        )
                        Button(
                            modifier = Modifier.size(
                                when (windowSizeClass.widthSizeClass) {
                                    WindowWidthSizeClass.Compact -> 28.dp
                                    WindowWidthSizeClass.Medium -> 32.dp
                                    else -> 36.dp
                                }
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            onClick = {
                                onFullscreen?.invoke(!isFullScreen)
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(
                                    when (windowSizeClass.widthSizeClass) {
                                        WindowWidthSizeClass.Compact -> 24.dp
                                        WindowWidthSizeClass.Medium -> 28.dp
                                        else -> 32.dp
                                    }
                                ),
                                imageVector = when (isFullScreen) {
                                    true -> Icons.Rounded.FullscreenExit
                                    false -> Icons.Rounded.Fullscreen
                                },
                                contentDescription = "",
                                tint = Color.White
                            )
                        }

                    }
                    val barHeight = when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Compact -> 6.dp
                        WindowWidthSizeClass.Medium -> 8.dp
                        else -> 9.dp
                    }
                    val thumbRadius = 1.1f * barHeight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = when (windowSizeClass.widthSizeClass) {
                                    WindowWidthSizeClass.Compact -> 15.dp
                                    WindowWidthSizeClass.Medium -> 17.dp
                                    else -> 20.dp
                                }
                            )
                            .padding(
                                top = when (windowSizeClass.widthSizeClass) {
                                    WindowWidthSizeClass.Compact -> 6.dp
                                    WindowWidthSizeClass.Medium -> 7.dp
                                    else -> 8.dp
                                },
                                bottom = when (windowSizeClass.widthSizeClass) {
                                    WindowWidthSizeClass.Compact -> 15.dp
                                    WindowWidthSizeClass.Medium -> 17.dp
                                    else -> 20.dp
                                }
                            )
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        val change = event.changes.firstOrNull() ?: continue
                                        when {
                                            !change.previousPressed && change.pressed -> { // Pointer down
                                                isInteracting = true
                                                val newProgress =
                                                    (change.position.x / size.width).coerceIn(0f..1f)
                                                val newPosition =
                                                    (newProgress * duration).toLong()
                                                onSeek(newPosition)
                                            }

                                            change.pressed && change.positionChange() != Offset.Zero -> { // Dragging
                                                isInteracting = true
                                                val newProgress =
                                                    (change.position.x / size.width).coerceIn(0f..1f)
                                                val newPosition =
                                                    (newProgress * duration).toLong()
                                                onSeek(newPosition)
                                            }

                                            change.previousPressed && !change.pressed -> { // Pointer up or Canceled
                                                isInteracting = false
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2 * thumbRadius)
                        ) {
                            val barHeightPx = barHeight.toPx()
                            val widthPx = size.width
                            val heightPx = size.height
                            val progress = position.toFloat() / duration
                            drawRoundRect(
                                color = Color(200f, 200f, 200f, 0.7f),
                                topLeft = Offset(0f, heightPx / 2f - barHeightPx / 2f),
                                size = Size(widthPx, barHeightPx),
                                cornerRadius = CornerRadius(barHeightPx)
                            )
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(0f, heightPx / 2f - barHeightPx / 2f),
                                size = Size(widthPx * progress, barHeightPx),
                                cornerRadius = CornerRadius(barHeightPx)
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 1.3f * barHeightPx,
                                center = Offset(widthPx * progress, heightPx / 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class Direction {
    Rtl, Ltr
}

const val degPerRad = 360 / (2 * PI.toFloat())

val Float.deg get() = times(degPerRad)
val Float.rad get() = div(degPerRad)

fun createStadiumShape(
    direction: Direction,
    radiusCoefficient: Float = 1.2f
): Shape {
    return GenericShape { size, _ ->
        val r = radiusCoefficient * max(size.width, size.height)
        val a = acos(1 - size.height.pow(2) / (2 * r.pow(2))).deg
        when (direction) {
            Direction.Ltr -> {
                moveTo(0f, 0f)
                arcTo(
                    rect = Rect(
                        topLeft = Offset(size.width - 2 * r, size.height / 2 - r),
                        bottomRight = Offset(size.width, size.height / 2 + r)
                    ),
                    startAngleDegrees = -a / 2,
                    sweepAngleDegrees = a,
                    forceMoveTo = false
                )
                lineTo(0f, size.height)
                lineTo(0f, 0f)
            }

            Direction.Rtl -> {
                moveTo(size.width, 0f)
                arcTo(
                    rect = Rect(
                        topLeft = Offset(0f, size.height / 2 - r),
                        bottomRight = Offset(2 * r, size.height / 2 + r)
                    ),
                    startAngleDegrees = 180 + a / 2,
                    sweepAngleDegrees = -a,
                    forceMoveTo = false
                )
                lineTo(size.width, size.height)
                lineTo(size.width, 0f)
            }
        }
        close()
    }
}

fun Long.formatDuration(): String {
    val totalSecs = (this + 500) / 1000
    val stringBuilder = StringBuilder()
    val secs = totalSecs % 60
    stringBuilder.insert(0, secs)
    if (secs < 10) {
        stringBuilder.insert(0, "0")
    }
    val mins = totalSecs / 60 % 60
    stringBuilder.insert(0, "$mins:")
    val hours = totalSecs / 60 / 60
    if (hours > 0) {
        if (mins < 10) {
            stringBuilder.insert(0, "0")
        }
        stringBuilder.insert(0, "$hours:")
    }
    return stringBuilder.toString()
}

@Composable
internal fun Player.isPrepared(): Boolean {
    var isPrepared by rememberSaveable {
        mutableStateOf(currentMediaItem != null)
    }
    DisposableEffect(Unit) {
        val playerListener = object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                val uri = mediaItem?.localConfiguration?.uri?.toString()
                isPrepared = uri != null
            }
        }
        addListener(playerListener)
        onDispose {
            isPrepared = currentMediaItem != null
            removeListener(playerListener)
        }
    }
    return isPrepared
}

@Composable
fun Player.rememberPlayerState(): MutableState<PlayerState> {
    val state = LocalPlayerState.current
    val isPrepared = isPrepared()
    LaunchedEffect(isPrepared) {
        state.value = state.value.copy(prepared = isPrepared)
    }
    return state
}

fun Modifier.shimmer(
    direction: Direction,
    baseColor: Color = Color(0xFF7F7E7E),
    accentColor: Color = Color(
        red = (baseColor.red * 1.6f).coerceAtMost(1f),
        green = (baseColor.green * 1.6f).coerceAtMost(1f),
        blue = (baseColor.blue * 1.6f).coerceAtMost(1f),
    )
): Modifier = composed {
    var width by remember {
        mutableFloatStateOf(0f)
    }
    val offsetX by rememberInfiniteTransition().animateFloat(
        initialValue = if (direction == Direction.Ltr) -2 * width else 2 * width,
        targetValue = if (direction == Direction.Ltr) 2 * width else -2 * width,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val background = Brush.horizontalGradient(
        colors = listOf(
            baseColor,
            accentColor,
            baseColor
        ),
        startX = offsetX,
        endX = width + offsetX
    )
    return@composed Modifier
        .background(background)
        .onGloballyPositioned {
            width = it.size.width.toFloat()
        }
}