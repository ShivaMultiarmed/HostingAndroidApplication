package mikhail.shell.video.hosting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.presentation.video.page.VideoScreen
import mikhail.shell.video.hosting.presentation.video.page.VideoScreenPreview
import mikhail.shell.video.hosting.presentation.video.page.VideoScreenViewModel
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoHostingTheme {
                val userId = 1L
                val videoId = 1L
                val context = LocalContext.current
                val player = ExoPlayer.Builder(context).build()
                val videoScreenViewModel =
                    hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
                        factory.create(player, userId, videoId)
                    }
                val state by videoScreenViewModel.state.collectAsState()
                VideoScreen(
                    state = state,
                    exoPlayerConnection = { context ->
                        PlayerView(context).also {
                            it.player = videoScreenViewModel.player
                        }
                    },
                    onRefresh = {
                        videoScreenViewModel.loadVideo()
                    },
                    onRate = {
                        videoScreenViewModel.rate(it)
                    },
                    onSubscribe = {

                    }
                )
            }
        }
    }
}
