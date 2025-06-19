package mikhail.shell.video.hosting.presentation.video

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import mikhail.shell.video.hosting.presentation.exoplayer.PlayerComponent
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.PipContainer
import mikhail.shell.video.hosting.presentation.utils.PipTopBar

@Composable
fun MiniPlayer(
    player: Player,
    isFullScreen: Boolean = false,
    onFullScreen: (videoId: Long) -> Unit
) {
    var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
    val maxDimension = 250.dp
    PipContainer(
        modifier = Modifier
            .then(
                if (aspectRatio < 1f) {
                    Modifier.height(maxDimension)
                } else {
                    Modifier.width(maxDimension)
                }
            )
            .aspectRatio(aspectRatio)
    ) {
        var topBarAlpha by rememberSaveable { mutableFloatStateOf(1f) }
        val animatedTopBarAlpha by animateFloatAsState(
            targetValue = topBarAlpha,
            animationSpec = tween(200),
            label = "pip container top bar animation"
        )
        PlayerComponent(
            modifier = Modifier.matchParentSize(),
            player = player,
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

@Composable
fun shouldShowMiniPlayer(navController: NavController): Boolean {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    return Route.Video.View::class.qualifiedName!! !in currentRoute.toString()
            && currentRoute != null
}