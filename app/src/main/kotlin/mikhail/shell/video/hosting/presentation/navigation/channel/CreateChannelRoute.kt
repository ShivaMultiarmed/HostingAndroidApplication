package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreen
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationUiEvent
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderBuilder<Route>.createChannelRoute(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>
) {
    entry <Route.User.CreateChannel> {
        val viewModel = hiltViewModel<ChannelCreationViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        ChannelCreationScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    is ChannelCreationUiEvent.Cancel -> rootBackStack.removeLastOrNull()
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state.error) {
            if (state.error == NetworkError.AUTHENTICATION) {
                rootBackStack.add(Route.Authentication)
            }
        }
        LaunchedEffect(state.channelId) {
            if (state.channelId != null) {
                userBackStack.add(Route.Channel(state.channelId!!))
                userBackStack.remove(Route.User.CreateChannel)
            }
        }
    }
}