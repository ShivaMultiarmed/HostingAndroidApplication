package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.PresentationModule.HOST
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreen
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
fun EntryProviderBuilder<Route>.videoRoute(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: MutableList<Route>,
    videoBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    // TODO navDeepLink (basePath = "https://$HOST/videos")
    entry <Route.Video.View> { route ->
        val context = LocalContext.current
        val videoId = route.videoId
        val coroutineScope = rememberCoroutineScope()
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { it.create(videoId, player) }
        val state by viewModel.state.collectAsState()
        VideoScreen(
            userId = userId,
            state = state,
            player = player,
            onEvent = { event ->
                when (event) {
                    VideoScreenUiEvent.Edit -> videoBackStack.add(Route.Video.Edit(videoId))
                    VideoScreenUiEvent.OpenChannel -> {
                        val channelId = state.video!!.channelId
                        currentTabBackStack.add(Route.Channel(channelId))
                        rootBackStack.removeLastOrNull()
                    }
                    is VideoScreenUiEvent.OpenProfile -> currentTabBackStack.add(Route.User.Profile(event.userId))
                    VideoScreenUiEvent.Remove -> {
                        coroutineScope.launch {
                            viewModel.onEvent(event)
                            delay(1.seconds)
                            videoBackStack.clear()
                        }
                    }
                    VideoScreenUiEvent.Share -> {
                        Intent(Intent.ACTION_SEND).apply {
                            setType("text/plain")
                            putExtra(Intent.EXTRA_TEXT, "https://$HOST/videos/$videoId")
                            context.startActivity(
                                Intent.createChooser(this, context.getString(R.string.video_share))
                            )
                        }
                    }
                    VideoScreenUiEvent.DownLoad -> {
                        Intent(context, VideoDownloadingService::class.java).also {
                            it.action = VideoDownloadingService.ACTION_LAUNCH_DOWNLOADING
                            it.putExtra("videoId", videoId)
                            context.startService(it)
                        }
                    }
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            when(state.startingError) {
                NetworkError.NOT_FOUND -> {
                    delay(1.seconds)
                    videoBackStack.remove(route)
                }
                NetworkError.AUTHENTICATION -> rootBackStack.add(Route.Authentication)
            }
            if (state.isRemoved) {
                videoBackStack.remove(route)
            }
        }
    }
}