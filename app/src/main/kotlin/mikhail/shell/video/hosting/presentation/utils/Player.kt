package mikhail.shell.video.hosting.presentation.utils

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView

@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    player: Player
) {
    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                it.player = player
            }
        }
    )
}