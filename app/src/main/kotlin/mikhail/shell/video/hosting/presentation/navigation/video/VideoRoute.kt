package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreen
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenViewModel

@OptIn(UnstableApi::class)
fun NavGraphBuilder.videoRoute(
    navController: NavController,
    player: Player,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Video.View> {
        val videoRouteInfo = it.toRoute<Route.Video.View>()
        val videoId = videoRouteInfo.videoId
        val coroutineScope = rememberCoroutineScope()
        val userId = userDetailsProvider.getUserId()
        val videoScreenViewModel =
            hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
                factory.create(userId, videoId, player)
            }
        val state by videoScreenViewModel.state.collectAsStateWithLifecycle()
        VideoScreen(
            userId = userId,
            state = state,
            player = player,
            onRefresh = videoScreenViewModel::loadVideo,
            onRate = videoScreenViewModel::rate,
            onSubscribe = videoScreenViewModel::subscribe,
            onChannelLinkClick = {
                navController.navigate(Route.Channel.View(it))
            },
            onView = videoScreenViewModel::incrementViews,
            onDelete = {
                videoScreenViewModel.deleteVideo()
                coroutineScope.launch {
                    delay(1000)
                    navController.navigate(Route.User)
                }
            },
            onUpdate = {
                coroutineScope.launch {
                    delay(1000)
                    navController.navigate(Route.Video.Edit(it))
                }
            },
            onComment = videoScreenViewModel::saveComment,
            onLoadComments = videoScreenViewModel::getComments,
            onObserve = videoScreenViewModel::observeComments,
            onUnobserve = videoScreenViewModel::unobserveComments,
            onRemoveComment = videoScreenViewModel::removeComment
        )

    }
}