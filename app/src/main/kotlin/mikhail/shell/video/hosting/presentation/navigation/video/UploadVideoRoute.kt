package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.VideoUploadingService
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreen
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingViewModel
import kotlin.time.Duration.Companion.milliseconds

fun EntryProviderBuilder<Route>.uploadVideoRoute(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.User.UploadVideo> {
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
            onEvent = { event ->
                when (event) {
                    VideoUploadingScreenUiEvent.Cancel -> userBackStack.removeLastOrNull()
                    is VideoUploadingScreenUiEvent.Success -> {
                        coroutineScope.launch {
                            if (!event.source.contains(context.packageName + ".fileprovider")) {
                                context.contentResolver.takePersistableUriPermission(event.source.toUri(), FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startService(
                                Intent(context, VideoUploadingService::class.java).also { intent ->
                                    intent.action = VideoUploadingService.ACTION_LAUNCH_UPLOADING
                                    intent.putExtra("source", event.source)
                                }
                            )
                            delay(1000.milliseconds)
                            userBackStack.add(Route.Channel(event.videoId))
                        }
                    }
                    else -> viewModel.onEvent(event)
                }
            }
        )
        if (state is VideoUploadingScreenState.Editing) {
            if ((state as VideoUploadingScreenState.Editing).error == NetworkError.AUTHENTICATION) {
                rootBackStack.add(Route.Authentication)
            }
        }
    }
}