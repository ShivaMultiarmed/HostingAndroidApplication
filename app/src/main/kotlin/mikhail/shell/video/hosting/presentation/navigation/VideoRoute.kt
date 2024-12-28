package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreen
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenViewModel

fun NavGraphBuilder.videoRoute(
    navController: NavController,
    dsFactory: DefaultMediaSourceFactory,
    userDetailsProvider: UserDetailsProvider,
) {
    composable<Route.Video> {
        val videoRouteInfo = it.toRoute<Route.Video>()
        val videoId = videoRouteInfo.videoId
        val context = LocalContext.current
        val videoScreenViewModel =
            hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
                val userId = userDetailsProvider.getUserId()
                val player = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(dsFactory)
                    .build()
                factory.create(player, userId, videoId)
            }
        val state by videoScreenViewModel.state.collectAsStateWithLifecycle()
        VideoScreen(
            state = state,
            player = videoScreenViewModel.player,
            onRefresh = {
                videoScreenViewModel.loadVideo()
            },
            onRate = {
                videoScreenViewModel.rate(it)
            },
            onSubscribe = {
                videoScreenViewModel.subscribe(it)
            },
            onChannelLinkClick = {
                navController.navigate(Route.Channel(it))
            }
        )
    }
}