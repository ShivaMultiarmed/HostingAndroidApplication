package mikhail.shell.video.hosting

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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
                            videoScreenViewModel.player.addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    when (playbackState) {
                                        Player.STATE_IDLE -> Log.d("ExoPlayer", "Player is idle")
                                        Player.STATE_BUFFERING -> Log.d("ExoPlayer", "Buffering")
                                        Player.STATE_READY -> Log.d("ExoPlayer", "Ready to play")
                                        Player.STATE_ENDED -> Log.d("ExoPlayer", "Playback ended")
                                    }
                                }

                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e("ExoPlayer", "Playback error: ${error.message}")
                                }
                            })
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
