package mikhail.shell.video.hosting.presentation.video

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import mikhail.shell.video.hosting.presentation.exoplayer.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.PipContainer
import mikhail.shell.video.hosting.presentation.utils.PipTopBar

@Composable
fun MiniPlayer(
    player: Player,
    isFullScreen: Boolean = false,
    onFullScreen: (videoId: Long) -> Unit
) {
    var pipWidth by remember { mutableStateOf(200.dp) }
    PipContainer(
        modifier = Modifier
            .wrapContentSize()
            .width(pipWidth)
    ) {
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
                    .fillMaxWidth()
                    .wrapContentHeight(),
                player = player,
                isFullScreen = isFullScreen,
                onFullscreen = {
                    val videoId = player.currentMediaItem
                        ?.localConfiguration?.uri?.toString()!!
                        .split("/").dropLast(1).last().toLong()
                    onFullScreen(videoId)
                }
            )
            PipTopBar(
                onClose = {
                    player.clearMediaItems()
                }
            )
        }
    }

}