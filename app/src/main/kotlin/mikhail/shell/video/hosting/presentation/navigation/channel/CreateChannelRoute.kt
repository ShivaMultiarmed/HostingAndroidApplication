package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationUiEvent
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationViewModel
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreen
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.createChannelRoute(
    navController: NavController
) {
    composable<Route.Channel.Create> {
        val viewModel = hiltViewModel<ChannelCreationViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ChannelCreationScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    is ChannelCreationUiEvent.Success -> navController.navigate(Route.Channel.View(event.channelId))
                    is ChannelCreationUiEvent.AuthenticationRequired -> coroutineScope.launch {
                        delay(800.milliseconds)
                        navController.navigate(Route.Authentication)
                    }
                    is ChannelCreationUiEvent.Cancel -> navController.popBackStack()
                    else -> viewModel.onEvent(event)
                }
            }
        )
    }
}