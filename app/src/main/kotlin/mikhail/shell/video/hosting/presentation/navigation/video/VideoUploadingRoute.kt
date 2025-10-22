package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.VideoUploadingService
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreen
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingViewModel

fun EntryProviderScope<Route>.videoUploadingRoute(
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
        VideoUploadingScreen(
            state = state,
            player = viewModel.player,
            onEvent = { event ->
                when (event) {
                    VideoUploadingScreenUiEvent.Cancel -> userBackStack.removeLastOrNull()
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is VideoUploadingScreenState.Editing) {
                if ((state as VideoUploadingScreenState.Editing).error == NetworkError.AUTHENTICATION) {
                    rootBackStack.add(Route.Authentication)
                }
            } else if (state is VideoUploadingScreenState.Success) {
                val successState = state as VideoUploadingScreenState.Success
                if (!successState.source.contains("${context.packageName}.fileprovider")) {
                    context.contentResolver.takePersistableUriPermission(successState.source.toUri(), FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startService(
                    Intent(context, VideoUploadingService::class.java).also { intent ->
                        intent.action = VideoUploadingService.ACTION_LAUNCH_UPLOADING
                        intent.putExtra("upload_id", successState.uploadId)
                        intent.putExtra("source", successState.source)
                    }
                )
                userBackStack.add(Route.Channel(successState.uploadId))
            }
        }
    }
}