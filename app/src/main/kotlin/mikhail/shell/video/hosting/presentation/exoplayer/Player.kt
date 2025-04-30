package mikhail.shell.video.hosting.presentation.exoplayer

import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    player: Player
) {
    var isPlaying by rememberSaveable { mutableStateOf(player.isPlaying) }
    var position by rememberSaveable { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var savedPlayState by rememberSaveable { mutableStateOf(player.isPlaying) }
    var aspectRatio by rememberSaveable { mutableFloatStateOf(16 / 9f) }
    var progressUpdatingJob: Job? = null
    val playerListener = remember {
        object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                aspectRatio = videoSize.pixelWidthHeightRatio
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

    Box(
        modifier = modifier
            .aspectRatio(if (aspectRatio > 1f) aspectRatio else 16f / 9)
            .background(MaterialTheme.colorScheme.onBackground),
    ) {
        var showControls by rememberSaveable { mutableFloatStateOf(0f) }
        val animatedShowControls by animateFloatAsState(
            targetValue = showControls,
            animationSpec = tween(
                durationMillis = 150
            )
        )
        AndroidView(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    coroutineScope.launch {
                        showControls = 1f
                        delay(3 * 1000)
                        showControls = 0f
                    }
                },
            factory = {
                PlayerView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
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
    onSeekForward: suspend () -> Unit
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
        Row(
            modifier = Modifier
                .fillMaxWidth()

                .constrainAs(seekBar) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            val density = LocalDensity.current
            BoxWithConstraints(
                modifier = Modifier
                    .height(50.dp)
                    .padding(10.dp)
                    .weight(1f)
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
                        topLeft = Offset(0f, height / 2f),
                        size = Size(width.toFloat(), barHeight),
                        cornerRadius = CornerRadius(barHeight)
                    )
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(0f, height / 2f),
                        size = Size(width * progress, barHeight),
                        cornerRadius = CornerRadius(barHeight)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 1.3f * barHeight,
                        center = Offset(width * progress, height / 2f + barHeight / 2)
                    )
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
            onSeek = {}
        )
    }
}