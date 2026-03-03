package mikhail.shell.video.hosting.presentation.video

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import mikhail.shell.video.hosting.presentation.player.LocalMiniPlayerDimensionsState
import mikhail.shell.video.hosting.presentation.player.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.PipContainer
import mikhail.shell.video.hosting.presentation.utils.PipTopBar

val miniPlayerMaxDimension = 220.dp

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
    var miniPlayerDimensions by LocalMiniPlayerDimensionsState.current
    val aspectRatio = miniPlayerDimensions?.aspectRatio?: (16f / 9)
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
                    aspectRatio.isNaN() || aspectRatio < 0 -> 16f / 9
                    else -> aspectRatio
                }
            )
            .zIndex(1000f),
        initialOffset = DpOffset(miniPlayerDimensions?.x?.dp?: 0.dp, miniPlayerDimensions?.y?.dp?: 0.dp),
        onOffsetChanged = { x, y ->
            miniPlayerDimensions = miniPlayerDimensions?.copy(
                x = x.value.toInt(),
                y = y.value.toInt()
            )
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
            onRatioObtained = { ratio ->
                miniPlayerDimensions = miniPlayerDimensions?.copy(aspectRatio = ratio)
            }
        )
        PipTopBar(
            modifier = Modifier
                .width(if (aspectRatio >= 1f) miniPlayerMaxDimension else miniPlayerMaxDimension * aspectRatio)
                .padding(7.dp),
            onClose = {
                miniPlayerDimensions = null
                player.stop()
                player.clearMediaItems()
            }
        )
    }
}