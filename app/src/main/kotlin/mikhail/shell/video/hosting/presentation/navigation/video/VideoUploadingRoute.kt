package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.services.VideoUploadingService
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.player.LocalPlayerState
import mikhail.shell.video.hosting.presentation.player.PlayerState
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreen
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingViewModel
import kotlin.uuid.ExperimentalUuidApi
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenEvent as ScreenEvent

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalUuidApi::class)
fun EntryProviderScope<Route>.videoUploadingRoute(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>
) {
    entry<Route.User.VideoUploading> {
        val context = LocalContext.current
        val playerState = rememberSerializable {
            mutableStateOf(PlayerState())
        }
        val viewModel =
            hiltViewModel<VideoUploadingViewModel, VideoUploadingViewModel.Factory> { factory ->
                val mediaSourceFactory: MediaSource.Factory = DefaultMediaSourceFactory(context)
                val player: Player = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build()
                factory.create(player)
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        CompositionLocalProvider(LocalPlayerState provides playerState) {
            VideoUploadingScreen(
                state = state,
                player = viewModel.player,
                onAction = viewModel::onAction,
                snackBarHostState = snackBarHostState
            )
        }
        events.observe { event ->
            when (event) {
                ScreenEvent.Cancelled -> userBackStack.removeLastOrNull()
                is ScreenEvent.Success -> {
                    if (!event.source.contains("${context.packageName}.fileprovider")) {
                        context.applicationContext.contentResolver.takePersistableUriPermission(
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

                is ScreenEvent.Failure -> {
                    if (event.error == NetworkError.AUTHENTICATION) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        context.getStandardErrorMessage(event.error)?.let {
                            snackBarHostState.showSnackbar(it)
                        }
                    }
                }

                is ScreenEvent.PermissionLacked -> {
                    snackBarHostState.showSnackbar(event.message)
                }
            }
        }
    }
}