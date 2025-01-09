package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoScreen
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoViewModel

fun NavGraphBuilder.uploadVideoRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider,
    dsFactory: DefaultMediaSourceFactory
) {
    composable<Route.UploadVideo> {
        val userId = userDetailsProvider.getUserId()
        val context = LocalContext.current
        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(dsFactory)
            .build()
        val viewModel =
            hiltViewModel<UploadVideoViewModel, UploadVideoViewModel.Factory> {
                it.create(userId, player)
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        UploadVideoScreen(
            state = state,
            player = player,
            onSubmit = {
                viewModel.uploadVideo(it)
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