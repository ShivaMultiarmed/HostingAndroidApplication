package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.PresentationModule.HOST
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.Route.Channel
import mikhail.shell.video.hosting.presentation.navigation.common.Route.User.Profile
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreen
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenEvent
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenViewModel

@OptIn(UnstableApi::class)
fun EntryProviderScope<Route>.videoRoute(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: MutableList<Route>,
    videoBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    // TODO navDeepLink (basePath = "https://$HOST/videos")
    entry <Route.Video.View> { route ->
        val context = LocalContext.current
        val videoId = rememberSaveable { route.videoId }
        val coroutineScope = rememberCoroutineScope()
        val userId = rememberSaveable { userDetailsProvider.getUserId() }
        val viewModel = hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
            factory.create(videoId, player)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        VideoScreen(
            userId = userId,
            state = state,
            player = player,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                VideoScreenEvent.EditRequested -> videoBackStack.add(Route.Video.Edit(videoId))
                VideoScreenEvent.ChannelRequested -> {
                    val channelId = state.video!!.channelId
                    currentTabBackStack.add(Channel(channelId))
                    rootBackStack.removeLastOrNull()
                }
                is VideoScreenEvent.ProfileRequested -> currentTabBackStack.add(Profile(event.userId))
                VideoScreenEvent.Removed -> rootBackStack.removeLastOrNull()
                VideoScreenEvent.SharingRequested -> {
                    Intent(Intent.ACTION_SEND).apply {
                        setType("text/plain")
                        putExtra(Intent.EXTRA_TEXT, "https://$HOST/videos/$videoId")
                        context.startActivity(
                            Intent.createChooser(this, context.getString(R.string.video_share))
                        )
                    }
                }
                VideoScreenEvent.DownloadRequested -> {
                    Intent(context, VideoDownloadingService::class.java).also {
                        it.action = VideoDownloadingService.ACTION_LAUNCH_DOWNLOADING
                        it.putExtra("videoId", videoId)
                        context.startService(it)
                    }
                }
                is VideoScreenEvent.Failure -> {
                    if (event.error == NetworkError.AUTHENTICATION) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        coroutineScope.launch {
                            context.getStandardErrorMessage(event.error)?.let {
                                snackBarHostState.showSnackbar(it)
                            }
                        }
                    }
                }
            }
        }
    }
}