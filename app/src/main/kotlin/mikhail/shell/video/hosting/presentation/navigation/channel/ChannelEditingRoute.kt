package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreen
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenState
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingUiEvent
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderBuilder<Route>.channelEditingRoute(
    rootBackStack: MutableList<Route>,
    channelBackStack: MutableList<Route>
) {
    entry <Route.Channel.Edit> { route ->
        val viewModel = hiltViewModel<ChannelEditingViewModel, ChannelEditingViewModel.Factory> { it.create(route.channelId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        ChannelEditingScreen(
            state = state,
            onEvent = {
                when (it) {
                    ChannelEditingUiEvent.Cancel -> channelBackStack.removeLastOrNull()
                    else -> viewModel.onEvent(it)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is ChannelEditingScreenState.Failure) {
                if ((state as ChannelEditingScreenState.Failure).error == NetworkError.NOT_FOUND) {
                    channelBackStack.clear()
                } else if ((state as ChannelEditingScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    rootBackStack.add(Route.Authentication)
                }
            } else if (state is ChannelEditingScreenState.Success) {
                channelBackStack.removeIf { it is Route.Channel.View }
                channelBackStack.remove(route)
                channelBackStack.add(Route.Channel.View(route.channelId))
            }
        }
    }
}