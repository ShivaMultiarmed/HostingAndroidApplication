package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observeAsEvents
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreen
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenEvent
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
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
        val snackBarHostState = remember { SnackbarHostState() }
        VideoUploadingScreen(
            state = state,
            player = viewModel.player,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        viewModel.events.observeAsEvents { event ->
            when (event) {
                VideoUploadingScreenEvent.RequireAuthentication -> {
                    rootBackStack.add(Route.Authentication)
                }
                VideoUploadingScreenEvent.NavigateBack -> userBackStack.removeLastOrNull()
                is VideoUploadingScreenEvent.Success -> {
                    if (!event.source.contains("${context.packageName}.fileprovider")) {
                        context.contentResolver.takePersistableUriPermission(
                            event.source.toUri(),
                            FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    context.startService(
                        Intent(context, VideoUploadingService::class.java).also { intent ->
                            intent.action = VideoUploadingService.ACTION_LAUNCH_UPLOADING
                            intent.putExtra("tmp_id", event.tmpId.toString())
                            intent.putExtra("source", event.source)
                        }
                    )
                    userBackStack.add(Route.Channel(event.channelId))
                }
                is VideoUploadingScreenEvent.Failure if (event.error is NetworkError) -> {
                    val errorMessage = context.getNetworkErrorMessage(event.error)
                    snackBarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                else -> Unit
            }
        }
    }
}