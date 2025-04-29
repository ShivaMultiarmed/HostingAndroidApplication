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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    player: Player
) {
    val context = LocalContext.current
    var savedPlayState by rememberSaveable { mutableStateOf(player.isPlaying) }
    var aspectRatio by rememberSaveable { mutableFloatStateOf(16 / 9f) }
    val playerListener = remember {
        object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                aspectRatio = videoSize.pixelWidthHeightRatio
            }
        }
    }
    Box(
        modifier = modifier
            .aspectRatio(if (aspectRatio > 1f) aspectRatio else 16f / 9)
            .background(MaterialTheme.colorScheme.onBackground),
    ) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
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
        PlayerControls(
            modifier = Modifier.matchParentSize(),
            player = player
        )
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

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    player: Player
) {
    val coroutineScope = rememberCoroutineScope()
    var showControls by rememberSaveable { mutableFloatStateOf(0f) }
    val animatedShowControls by animateFloatAsState(
        targetValue = showControls,
        animationSpec = tween(
            durationMillis = 100
        )
    )
    ConstraintLayout(
        modifier = modifier
            .clickable {
                coroutineScope.launch {
                    showControls = 1f
                    delay(3 * 1000)
                    showControls = 0f
                }
            }
    ) {
        val (playBtn, seekBack, seekForward, seekBar) = createRefs()
        IconButton(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .constrainAs(playBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            onClick = {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                tint = Color.Black,
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
                .background(
                    Color(255f, 255f, 255f, 0.3f * animatedSeekBackProgress)
                )
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        coroutineScope.launch {
                            seekBackProgress = 1f
                            val newPosition = (player.currentPosition - 5 * 1000).coerceAtLeast(0)
                            player.seekTo(newPosition)
                            delay(1000)
                            seekBackProgress = 0f
                        }

                    }
                )
        )
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
                .background(
                    Color(255f, 255f, 255f, 0.3f * animatedSeekForwardProgress)
                )
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        coroutineScope.launch {
                            seekForwardProgress = 1f
                            val newPosition = (player.currentPosition + 5 * 1000).coerceAtMost(player.duration - 1)
                            player.seekTo(newPosition)
                            delay(1500)
                            seekForwardProgress = 0f
                        }
                    }
                )
        )
    }
}

enum class ShapeDirection {
    Rtl, Ltr
}

fun createStadiumShape(direction: ShapeDirection): Shape {
    return GenericShape { size, _ ->
        val radius = size.height / 1.5f
        if (direction == ShapeDirection.Ltr) {
            moveTo(0f, 0f)
            lineTo(size.width - radius / 1.2f, 0f)
            arcTo(
                rect = Rect(
                    size.width - 2 * radius,
                    0f,
                    size.width,
                    size.height
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            lineTo(0f, size.height)
            lineTo(0f, 0f)
        } else {
            moveTo(size.width, 0f)
            lineTo(radius / 1.2f, 0f)
            arcTo(
                rect = Rect(
                    0f,
                    0f,
                    2 * radius,
                    size.height
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