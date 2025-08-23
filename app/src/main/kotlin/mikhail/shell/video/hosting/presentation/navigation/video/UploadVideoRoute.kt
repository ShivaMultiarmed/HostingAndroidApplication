package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreen
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingViewModel
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.uploadVideoRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Video.Upload> {
        val userId = userDetailsProvider.getUserId()
        val context = LocalContext.current
        val viewModel = hiltViewModel<VideoUploadingViewModel, VideoUploadingViewModel.Factory> {
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
            val player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
            it.create(userId, player)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        VideoUploadingScreen(
            state = state,
            player = viewModel.player,
            onEvent = {
                when (it) {
                    VideoUploadingScreenUiEvent.Cancel -> navController.popBackStack()
                    is VideoUploadingScreenUiEvent.Success -> {
                        coroutineScope.launch {
                            if (!it.source.contains(context.packageName + ".fileprovider")) {
                                context.contentResolver.takePersistableUriPermission(it.source.toUri(), FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startService(
                                Intent(context, VideoUploadingService::class.java).also { intent ->
                                    intent.action = VideoUploadingService.ACTION_LAUNCH_UPLOADING
                                    intent.putExtra("source", it.source)
                                }
                            )
                            delay(1000.milliseconds)
                            navController.navigate(Route.User.Profile(userId))
                        }
                    }
                    else -> viewModel.onEvent(it)
                }
            }
        )

    }
}