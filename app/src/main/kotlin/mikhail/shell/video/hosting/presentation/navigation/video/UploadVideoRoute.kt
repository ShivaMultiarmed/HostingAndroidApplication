package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
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
import mikhail.shell.video.hosting.domain.services.VideoUploadingService
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoScreen
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoViewModel

fun NavGraphBuilder.uploadVideoRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider,
    onFullScreen: (Boolean) -> Unit
) {
    composable<Route.Video.Upload> {
        val userId = userDetailsProvider.getUserId()
        val context = LocalContext.current
        val viewModel = hiltViewModel<UploadVideoViewModel, UploadVideoViewModel.Factory> {
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
            val player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
            it.create(userId, player)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        UploadVideoScreen(
            state = state,
            player = viewModel.player,
            onSubmit = { input ->
                if (viewModel.validateVideoInput(input) == null) {
                    val sourceUri = input.source!!
                    if (!sourceUri.toString().contains(context.packageName + ".fileprovider")) {
                        context.contentResolver.takePersistableUriPermission(sourceUri, FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val coverUri = input.cover
                    coverUri?.let {
                        context.contentResolver.takePersistableUriPermission(it, FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    coroutineScope.launch {
                        delay(1000)
                        context.startService(
                            Intent(
                                context,
                                VideoUploadingService::class.java
                            ).also {
                                it.putExtra("channelId", input.channelId)
                                it.putExtra("title", input.title)
                                it.putExtra("source", input.source.toString())
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
            onRefresh = viewModel::loadChannels,
            onPopup = navController::popBackStack,
            onFullScreen = onFullScreen
        )

    }
}