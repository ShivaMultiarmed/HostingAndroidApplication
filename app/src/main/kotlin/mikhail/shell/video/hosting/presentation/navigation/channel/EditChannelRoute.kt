package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreen
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenState
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingUiEvent
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.editChannelRoute(
    navController: NavController
) {
    composable<Route.Channel.Edit> {
        val data = it.toRoute<Route.Channel.Edit>()
        val viewModel = hiltViewModel<ChannelEditingViewModel, ChannelEditingViewModel.Factory> { it.create(data.channelId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        ChannelEditingScreen(
            state = state,
            onEvent = {
                when (it) {
                    ChannelEditingUiEvent.Cancel -> navController.popBackStack()
                    else -> viewModel.onEvent(it)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is ChannelEditingScreenState.Failure) {
                if ((state as ChannelEditingScreenState.Failure).error == NetworkError.NOT_FOUND) {
                    navController.popBackStack()
                } else if ((state as ChannelEditingScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    navController.navigate(Route.Authentication)
                }
            } else if (state is ChannelEditingScreenState.Success) {
                navController.navigate(Route.Channel.View(data.channelId))
            }
        }
    }
}