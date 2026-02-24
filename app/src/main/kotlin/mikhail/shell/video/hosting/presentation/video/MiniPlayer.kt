package mikhail.shell.video.hosting.presentation.video

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.ui.PlayerView
import mikhail.shell.video.hosting.presentation.player.LocalMiniPlayerPositionState
import mikhail.shell.video.hosting.presentation.player.MiniPlayerPosition
import mikhail.shell.video.hosting.presentation.player.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.PipContainer
import mikhail.shell.video.hosting.presentation.utils.PipTopBar

val miniPlayerMaxDimension = 220.dp

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    playerViewProvider: () -> PlayerView,
    isFullScreen: Boolean = false,
    onFullScreen: (videoId: Long) -> Unit
) {
    val playerView = remember {
        playerViewProvider()
    }
    val player = retain {
        playerView.player!!
    }
    var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
    var miniPlayerPosition by LocalMiniPlayerPositionState.current
    PipContainer(
        modifier = modifier
            .then(
                if (aspectRatio < 1f) {
                    Modifier.height(miniPlayerMaxDimension)
                } else {
                    Modifier.width(miniPlayerMaxDimension)
                }
            ).aspectRatio(
                when {
                    !aspectRatio.isNaN() && aspectRatio > 0 -> aspectRatio
                    else -> 16f / 9
                }
            )
            .zIndex(1000f),
        initialOffset = DpOffset(miniPlayerPosition.x.dp, miniPlayerPosition.y.dp),
        onOffsetChanged = { x, y ->
            miniPlayerPosition = MiniPlayerPosition(x.value.toInt(), y.value.toInt())
        }
    ) {
        PlayerComponent(
            modifier = Modifier.matchParentSize(),
            playerViewProvider = playerViewProvider,
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
                .width(if (aspectRatio >= 1f) miniPlayerMaxDimension else miniPlayerMaxDimension * aspectRatio)
                .padding(7.dp),
            onClose = {
                player.stop()
                player.clearMediaItems()
            }
        )
    }
}