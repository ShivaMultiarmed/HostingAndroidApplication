package mikhail.shell.video.hosting.presentation.exoplayer

import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@OptIn(UnstableApi::class)
@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    player: Player,
    isFullScreen: Boolean = false,
    onFullscreen: ((Boolean) -> Unit)? = null,
    onRatioObtained: (ratio: Float) -> Unit = {}
) {
    var isPlaying by rememberSaveable { mutableStateOf(player.isPlaying) }
    var position by rememberSaveable { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var savedPlayState by rememberSaveable { mutableStateOf(player.isPlaying) }
    var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
    val orientation = LocalConfiguration.current.orientation
    var progressUpdatingJob: Job? = null
    val playerListener = remember {
        object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                aspectRatio = videoSize.width.toFloat() / videoSize.height
                onRatioObtained(aspectRatio)
            }
            override fun onIsPlayingChanged(newIsPlaying: Boolean) {
                isPlaying = newIsPlaying
            }
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                position = newPosition.positionMs
            }
        }
    }
    var showControls by rememberSaveable { mutableFloatStateOf(0f) }
    val animatedShowControls by animateFloatAsState(
        targetValue = showControls,
        animationSpec = tween(
            durationMillis = 150
        )
    )
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable (
                indication = null,
                interactionSource = interactionSource
            ) {
                coroutineScope.launch {
                    showControls = 1f
                    delay(3 * 1000)
                    showControls = 0f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier,
            factory = {
                PlayerView(it).apply {
                    useController = false
                    this.player = player
                    this.player!!.addListener(playerListener)
                }
            },
            onRelease = {
                it.player!!.removeListener(playerListener)
            }
        )

        if (animatedShowControls > 0f) {
            PlayerControls(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(animatedShowControls),
                position = position,
                duration = player.duration,
                isPlaying = isPlaying,
                onPlay = player::play,
                onPause = player::pause,
                onSeekBack = {
                    val newPosition = (player.currentPosition - 5 * 1000).coerceAtLeast(0)
                    player.seekTo(newPosition)
                    delay(500)
                },
                onSeekForward = {
                    val newPosition =
                        (player.currentPosition + 5 * 1000).coerceAtMost(player.duration - 1)
                    player.seekTo(newPosition)
                    delay(1500)
                },
                onSeek = {
                    player.seekTo(it)
                    delay(300)
                },
                isFullScreen = isFullScreen,
                onFullscreen = onFullscreen
            )
        }
    }
    LaunchedEffect(Unit) {
         progressUpdatingJob = coroutineScope.launch {
            while(true) {
                position = player.currentPosition
                delay(1000)
            }
        }
    }
    DisposableEffect(Unit) {
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

        onDispose {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
    }
    BackHandler(enabled = isFullScreen) {
        onFullscreen?.invoke(false)
    }
}

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: suspend (Long) -> Unit,
    onSeekBack: suspend () -> Unit,
    onSeekForward: suspend () -> Unit,
    isFullScreen: Boolean = false,
    onFullscreen: ((Boolean) -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    ConstraintLayout(
        modifier = modifier
    ) {
        val (playBtn, seekBack, seekForward, seekBar) = createRefs()
        IconButton(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(255f, 255f, 255f, 0.7f))
                .constrainAs(playBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            onClick = {
                if (isPlaying) {
                    onPause()
                } else {
                    onPlay()
                }
            }
        ) {
            Icon(
                imageVector = when (isPlaying) {
                    true -> Icons.Rounded.Pause
                    false -> Icons.Rounded.PlayArrow
                },
                tint = Color(0f, 0f, 0f, 0.8f),
                modifier = Modifier.size(28.dp),
                contentDescription = "Кнопка проигрывания"
            )
        }
        var seekBackProgress by rememberSaveable { mutableFloatStateOf(0f) }
        val animatedSeekBackProgress by animateFloatAsState(
            targetValue = seekBackProgress,
            animationSpec = tween(
                durationMillis = 150
            )
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(0.35f)
                .constrainAs(seekBack) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .clip(createStadiumShape(ShapeDirection.Ltr))
                .alpha(animatedSeekBackProgress)
                .background(
                    Color(255f, 255f, 255f, 0.3f)
                )
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        coroutineScope.launch {
                            seekBackProgress = 1f
                            launch {
                                onSeekBack()
                            }
                            delay(150)
                            seekBackProgress = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                tint = Color(0f, 0f, 0f, 0.6f),
                imageVector = Icons.Rounded.FastRewind,
                contentDescription = "Назад"
            )
        }
        var seekForwardProgress by rememberSaveable { mutableFloatStateOf(0f) }
        val animatedSeekForwardProgress by animateFloatAsState(
            targetValue = seekForwardProgress,
            animationSpec = tween(
                durationMillis = 150
            )
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(0.35f)
                .constrainAs(seekForward) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .clip(createStadiumShape(ShapeDirection.Rtl))
                .alpha(animatedSeekForwardProgress)
                .background(
                    Color(255f, 255f, 255f, 0.3f)
                )
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        coroutineScope.launch {
                            seekForwardProgress = 1f
                            launch {
                                onSeekForward()
                            }
                            delay(150)
                            seekForwardProgress = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                tint = Color(0f, 0f, 0f, 0.6f),
                imageVector = Icons.Rounded.FastForward,
                contentDescription = "Вперёд"
            )
        }
        if (duration >= 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(seekBar) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentPositionString = millisToDurationString(position)
                    val overallDurationString = millisToDurationString(duration)
                    Text(
                        text = "$currentPositionString / $overallDurationString",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    if (onFullscreen != null) {
                        IconButton(
                            onClick = {
                                onFullscreen(!isFullScreen)
                            }
                        ) {
                            Icon(
                                imageVector = when(isFullScreen) {
                                    true -> Icons.Rounded.FullscreenExit
                                    false -> Icons.Rounded.Fullscreen
                                },
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .padding(horizontal = 15.dp)
                ) {
                    val width = constraints.maxWidth
                    val height = constraints.maxHeight
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val progress = position.toFloat() / duration
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val newProgress = (change.position.x / size.width).coerceIn(0f..1f)
                                    val newPosition = (newProgress * duration).toLong()
                                    coroutineScope.launch {
                                        onSeek(newPosition)
                                    }
                                }
                            }
                    ) {
                        val barHeight = 12f
                        drawRoundRect(
                            color = Color(200f, 200f, 200f, 0.7f),
                            topLeft = Offset(0f, height / 2f - barHeight * 3f),
                            size = Size(width.toFloat(), barHeight),
                            cornerRadius = CornerRadius(barHeight)
                        )
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(0f, height / 2f - barHeight * 3f),
                            size = Size(width * progress, barHeight),
                            cornerRadius = CornerRadius(barHeight)
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 1.3f * barHeight,
                            center = Offset(width * progress, height / 2f - barHeight * 3f + barHeight / 2)
                        )
                    }
                }
            }
        }
    }
}

enum class ShapeDirection {
    Rtl, Ltr
}

fun createStadiumShape(direction: ShapeDirection): Shape {
    return GenericShape { size, _ ->
        val diameter = size.height / 1.5f
        if (direction == ShapeDirection.Ltr) {
            moveTo(0f, 0f)
            lineTo(size.width - diameter / 1.2f, 0f)
            arcTo(
                rect = Rect(
                    size.width - diameter,
                    -0.2f * size.height,
                    size.width,
                    1.2f * size.height
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            lineTo(0f, size.height)
            lineTo(0f, 0f)
        } else {
            moveTo(size.width, 0f)
            lineTo(diameter / 1.2f, 0f)
            arcTo(
                rect = Rect(
                    0f,
                    -0.2f * size.height,
                    diameter,
                    1.2f * size.height
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -180f,
                forceMoveTo = true
            )
            lineTo(size.width, size.height)
            lineTo(size.width, 0f)
        }
        close()
    }
}

fun millisToDurationString(millis: Long): String {
    val totalSecs = millis / 1000
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
@Preview
fun PlayerControlsPreview() {
    VideoHostingTheme {
        PlayerControls(
            isPlaying = false,
            onPlay = {},
            onPause = {},
            onSeekForward = {},
            onSeekBack = {},
            position = 0,
            duration = 100500,
            onSeek = {},
            onFullscreen = {}
        )
    }
}