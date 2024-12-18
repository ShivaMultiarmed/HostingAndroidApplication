package mikhail.shell.video.hosting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
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
                val videoScreenViewModel = hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
                    factory.create(userId, videoId)
                }
                val state by videoScreenViewModel.state.collectAsState()
                VideoScreen(
                    state = state,
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
