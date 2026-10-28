package mikhail.shell.video.hosting.presentation.video

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import mikhail.shell.video.hosting.presentation.player.LocalMiniPlayerDimensionsState
import mikhail.shell.video.hosting.presentation.player.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.FloatingContainer
import mikhail.shell.video.hosting.presentation.utils.FloatingContainerTopBar

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    playerProvider: () -> Player,
    isFullScreen: Boolean = false,
    onFullScreen: (videoId: Long) -> Unit,
    controlsImage: Boolean = true
) {
    val player = retain {
        playerProvider()
    }
    var miniPlayerDimensions by LocalMiniPlayerDimensionsState.current
    var aspectRatio by rememberSaveable {
        mutableFloatStateOf(miniPlayerDimensions?.aspectRatio ?: (16f / 9))
    }
    var parentWidth by remember {
        mutableStateOf(0.dp)
    }
    var parentHeight by remember {
        mutableStateOf(0.dp)
    }
    val (width, height) = LocalWindowInfo.current.containerDpSize
    var miniPlayerMaxDimension by remember(width, height) {
        mutableStateOf(min(width, height) * 0.5f)
    }
    val density = LocalDensity.current
    FloatingContainer(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val parentSize = coordinates.parentLayoutCoordinates?.size
                with(density) {
                    parentWidth = parentSize?.width?.toDp()?: 0.dp
                    parentHeight = parentSize?.height?.toDp()?: 0.dp
                }
                miniPlayerMaxDimension = min(parentWidth, parentHeight) * 0.5f
            }
            .then(
                if (aspectRatio < 1f) {
                    Modifier.height(miniPlayerMaxDimension)
                } else {
                    Modifier.width(miniPlayerMaxDimension)
                }
            )
            .aspectRatio(
                when {
                    aspectRatio.isNaN() || aspectRatio < 0 -> 16f / 9
                    else -> aspectRatio
                }
            )
            .zIndex(1f),
        initialOffset = DpOffset(
            miniPlayerDimensions?.x?.dp ?: 0.dp,
            miniPlayerDimensions?.y?.dp ?: 0.dp
        ),
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
                if (!ratio.isNaN()) {
                    if (player.currentMediaItem != null) {
                        aspectRatio = ratio
                    }
                    miniPlayerDimensions = miniPlayerDimensions?.copy(aspectRatio = ratio)
                }
            },
            controlsImage = controlsImage
        )
        FloatingContainerTopBar(
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