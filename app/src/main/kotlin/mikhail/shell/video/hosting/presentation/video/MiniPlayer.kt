package mikhail.shell.video.hosting.presentation.video

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import mikhail.shell.video.hosting.presentation.utils.PipContainer
import mikhail.shell.video.hosting.presentation.utils.PlayerComponent

@Composable
fun MiniPlayer(
    player: Player,
    shouldPlay: Boolean,
    onOpenUp: (videoId: Long) -> Unit
) {
    var isPrepared by rememberSaveable { mutableStateOf(false) }
    player.addListener(
        object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                val uri = mediaItem?.localConfiguration?.uri?.toString()
                isPrepared = uri != null
            }
        }
    )
    if (isPrepared && shouldPlay) {
        PipContainer(
            modifier = Modifier.wrapContentSize()
        ) {
            val pipWidth = 200.dp
            // TODO
            var topBarAlpha by rememberSaveable { mutableFloatStateOf(1f) }
            val animatedTopBarAlpha by animateFloatAsState(
                targetValue = topBarAlpha,
                animationSpec = tween(200),
                label = "pip container top bar animation"
            )
            Box(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                PlayerComponent(
                    modifier = Modifier
                        .width(pipWidth)
                        .wrapContentHeight(),
                    player = player
                )
                Row(
                    modifier = Modifier
                        .width(pipWidth)
                        .padding(7.dp)
                        .alpha(animatedTopBarAlpha),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val videoId = player.currentMediaItem
                        ?.localConfiguration?.uri?.toString()!!
                        .split("/").dropLast(1).last().toLong()
                    Icon(
                        modifier = Modifier
                            .size(27.dp)
                            .clickable {
                                onOpenUp(videoId)
                            },
                        imageVector = Icons.Rounded.OpenInFull,
                        tint = Color.White,
                        contentDescription = "Открыть полностью"
                    )
                    Icon(
                        modifier = Modifier
                            .size(27.dp)
                            .clickable {
                                player.clearMediaItems()
                            },
                        imageVector = Icons.Rounded.Close,
                        tint = Color.White,
                        contentDescription = "Закрыть видео"
                    )
                }
            }
        }
    }
}