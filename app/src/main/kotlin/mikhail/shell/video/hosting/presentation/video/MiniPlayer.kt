package mikhail.shell.video.hosting.presentation.video

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import mikhail.shell.video.hosting.presentation.player.LocalMiniPlayerPositionState
import mikhail.shell.video.hosting.presentation.player.MiniPlayerPosition
import mikhail.shell.video.hosting.presentation.player.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.PipContainer
import mikhail.shell.video.hosting.presentation.utils.PipTopBar

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    playerProvider: () -> Player,
    isFullScreen: Boolean = false,
    onFullScreen: (videoId: Long) -> Unit
) {
    val player = retain {
        playerProvider()
    }
    var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
    val maxDimension = 250.dp
    var miniPlayerPosition by LocalMiniPlayerPositionState.current
    PipContainer(
        modifier = modifier
            .then(
                if (aspectRatio < 1f) {
                    Modifier.height(maxDimension)
                } else {
                    Modifier.width(maxDimension)
                }
            )
            .aspectRatio(aspectRatio),
        initialOffset = DpOffset(miniPlayerPosition.x.dp, miniPlayerPosition.y.dp),
        onOffsetChanged = { x, y ->
            miniPlayerPosition = MiniPlayerPosition(x.value.toInt(), y.value.toInt())
        }
    ) {
        PlayerComponent(
            modifier = Modifier.matchParentSize(),
            playerProvider = playerProvider,
            isFullScreen = isFullScreen,
            onFullscreen = {
                val videoId = player.currentMediaItem
                    ?.localConfiguration?.uri?.toString()!!
                    .split("/").dropLast(1).last().toLong()
                onFullScreen(videoId)
            },
            onRatioObtained = {
                aspectRatio = it
            }
        )
        PipTopBar(
            modifier = Modifier
                .width(maxDimension * aspectRatio.coerceAtMost(1f))
                .padding(7.dp),
            onClose = {
                player.stop()
                player.clearMediaItems()
            }
        )
    }
}