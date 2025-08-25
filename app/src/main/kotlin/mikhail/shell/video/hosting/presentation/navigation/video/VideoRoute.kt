package mikhail.shell.video.hosting.presentation.navigation.video

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.PresentationModule.HOST
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreen
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenState
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
fun NavGraphBuilder.videoRoute(
    navController: NavController,
    player: Player,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Video.View>(
        deepLinks = listOf(
            navDeepLink<Route.Video.View>(basePath = "https://$HOST/videos")
        )
    ) {
        val context = LocalContext.current
        val videoRouteInfo = it.toRoute<Route.Video.View>()
        val videoId = videoRouteInfo.videoId
        val coroutineScope = rememberCoroutineScope()
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { it.create(videoId, player) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VideoScreen(
            userId = userId,
            state = state,
            player = player,
            onEvent = {
                when (it) {
                    VideoScreenUiEvent.Edit -> navController.navigate(Route.Video.Edit(videoId))
                    VideoScreenUiEvent.OpenChannel -> {
                        val channelId = (state as? VideoScreenState.Success)?.video?.channelId!!
                        navController.navigate(Route.Channel.View(channelId))
                    }
                    is VideoScreenUiEvent.OpenProfile -> navController.navigate(Route.User.Profile(it.userId))
                    VideoScreenUiEvent.Remove -> {
                        coroutineScope.launch {
                            viewModel.onEvent(it)
                            delay(0.8.seconds)
                            navController.navigate(Route.User)
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
                            it.action =
                                "mikhail.shell.video.hosting.ACTION_LAUNCH_DOWNLOADING"
                            it.putExtra("videoId", videoId)
                            context.startService(it)
                        }
                    }
                    else -> viewModel.onEvent(it)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is VideoScreenState.Failure) {
                if ((state as VideoScreenState.Failure).error == NetworkError.NOT_FOUND) {
                    navController.popBackStack()
                } else if ((state as VideoScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    navController.navigate(Route.Authentication)
                }
            }
        }
    }
}