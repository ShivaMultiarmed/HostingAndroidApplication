package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.VideoUploadingService
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoScreen
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoViewModel

fun NavGraphBuilder.uploadVideoRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Video.Upload> {
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

                        context.startService(
                            Intent(
                                context,
                                VideoUploadingService::class.java
                            ).also {
                                it.putExtra("channelId", input.channelId)
                                it.putExtra("title", input.title)
                                it.putExtra("source", input.source!!.toString())
                                it.putExtra("cover", input.cover?.toString())
                            }
                        )
                        navController.navigate(Route.Channel.View(input.channelId!!))
                    }
                }
            },
            onSuccess = {
                coroutineScope.launch {
                    delay(1000)
                    navController.navigate(Route.Video.View(it.videoId!!))
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