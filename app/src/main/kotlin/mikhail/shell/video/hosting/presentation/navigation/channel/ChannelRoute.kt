package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.navigation.Route

fun NavGraphBuilder.channelRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Channel.View> {
        val channelRouteInfo = it.toRoute<Route.Channel.View>()
        val userId = userDetailsProvider.getUserId()
        val channelId = channelRouteInfo.channelId
        val viewModel =
            hiltViewModel<ChannelScreenViewModel, ChannelScreenViewModel.Factory> {
                it.create(channelId, userId)
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        ChannelScreen(
            state = state,
            onRefresh = {
                viewModel.loadChannelInfo()
                viewModel.loadVideosPart()
            },
            onSubscription = {
                viewModel.subscribe(it)
            },
            onVideoClick = {
                navController.navigate(Route.Video.View(it))
            },
            onScrollToBottom = {
                if (!viewModel.areAllVideosLoaded()) {
                    viewModel.loadVideosPart()
                }
            }
        )
    }
}