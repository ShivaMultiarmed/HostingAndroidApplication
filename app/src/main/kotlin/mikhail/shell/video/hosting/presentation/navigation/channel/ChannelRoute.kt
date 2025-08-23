package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenState
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenUiEvent
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.channelRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Channel.View> {
        val channelRouteInfo = it.toRoute<Route.Channel.View>()
        val userId = userDetailsProvider.getUserId()
        val channelId = channelRouteInfo.channelId
        val viewModel = hiltViewModel<ChannelScreenViewModel, ChannelScreenViewModel.Factory> { it.create(channelId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ChannelScreen(
            state = state,
            onEvent = {
                when (it) {
                    is ChannelScreenUiEvent.ClickVideo -> navController.navigate(Route.Video.View(it.videoId))
                    ChannelScreenUiEvent.Edit -> navController.navigate(Route.Channel.Edit(channelId))
                    ChannelScreenUiEvent.Remove -> coroutineScope.launch {
                        viewModel.onEvent(it)
                        delay(800.milliseconds)
                        navController.navigate(Route.User.Profile(userId))
                    }
                    else -> viewModel.onEvent(it)
                }
            },
            onChannelNotFound = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.popBackStack()
                }
            },
            onAuthenticationRequired = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.navigate(Route.Authentication)
                }
            },
            owns = userId == (state as? ChannelScreenState.Success)?.channel?.ownerId
        )
    }
}