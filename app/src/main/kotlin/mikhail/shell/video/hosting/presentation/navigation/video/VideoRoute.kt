package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.PresentationModule.HOST
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreen
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenViewModel
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenEvent as ScreenEvent

@OptIn(UnstableApi::class)
fun EntryProviderScope<Route>.videoRoute(
    rootBackStack: MutableList<Route>,
    videoBackStack: MutableList<Route>,
    currentTabBackStack: State<MutableList<Route>>
) {
    entry<Route.Video.View> { route ->
        val context = LocalContext.current
        val videoId = rememberSaveable { route.videoId }
        val viewModel =
            hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
                factory.create(videoId)
            }
        val player = viewModel.player
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        VideoScreen(
            state = state,
            player = player,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                ScreenEvent.EditRequested -> videoBackStack.add(Route.Video.Edit(videoId))
                ScreenEvent.ChannelRequested -> {
                    val channelId = state.video!!.channelId
                    currentTabBackStack.value.add(Route.Channel(channelId))
                    rootBackStack.removeLastOrNull()
                }
                is ScreenEvent.ProfileRequested -> {
                    currentTabBackStack.value.add(Route.User(event.userId))
                    rootBackStack.removeLastOrNull()
                }
                ScreenEvent.Removed -> {
                    player.stop()
                    player.clearMediaItems()
                    rootBackStack.removeLastOrNull()
                }
                ScreenEvent.SharingRequested -> {
                    Intent(Intent.ACTION_SEND).apply {
                        setType("text/plain")
                        putExtra(Intent.EXTRA_TEXT, "https://$HOST/videos/$videoId")
                        context.startActivity(
                            Intent.createChooser(this, context.getString(R.string.video_share))
                        )
                    }
                }
                ScreenEvent.DownloadRequested -> {
                    Intent(context, VideoDownloadingService::class.java).also {
                        it.action = VideoDownloadingService.ACTION_LAUNCH_DOWNLOADING
                        it.putExtra("videoId", videoId)
                        context.startService(it)
                    }
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
            }
        }
    }
}