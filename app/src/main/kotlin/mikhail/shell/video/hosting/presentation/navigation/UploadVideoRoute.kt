package mikhail.shell.video.hosting.presentation.navigation

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.VideoSourceUploadingService
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoScreen
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoViewModel

fun NavGraphBuilder.uploadVideoRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.UploadVideo> {
        val userId = userDetailsProvider.getUserId()
        val context = LocalContext.current
        val player = ExoPlayer.Builder(context).build()
        val viewModel =
            hiltViewModel<UploadVideoViewModel, UploadVideoViewModel.Factory> {
                it.create(userId, player)
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        UploadVideoScreen(
            state = state,
            player = player,
            onSubmit = { input ->
                if (viewModel.validateVideoInput(input) == null) {
                    coroutineScope.launch {
                        delay(1000)
                        Toast.makeText(
                            context,
                            "Когда видео загрузится, вы увидите уведомление.",
                            Toast.LENGTH_LONG
                        ).show()
                        context.startService(
                            Intent(
                                context,
                                VideoSourceUploadingService::class.java
                            ).also {
                                it.putExtra("channelId", input.channelId)
                                it.putExtra("title", input.title)
                                it.putExtra("source", input.source!!.absolutePath)
                                it.putExtra("cover", input.cover?.absolutePath)
                            })
                        navController.navigate(Route.Channel(input.channelId!!))
                    }
                }
            },
            onSuccess = {
                coroutineScope.launch {
                    delay(1000)
                    navController.navigate(Route.Video(it.videoId!!))
                }
            },
            onRefresh = {
                viewModel.loadChannels()
            },
            onPopup = {
                navController.popBackStack()
            }
        )

    }
}