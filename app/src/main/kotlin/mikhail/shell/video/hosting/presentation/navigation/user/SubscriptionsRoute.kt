package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreen
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenUiEvent
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenViewModel

fun EntryProviderBuilder<Route>.subscriptionsRoute(
    rootBackStack: MutableList<Route>,
    subscriptionsBackStack: MutableList<Route>
) {
    entry <Route.Subscriptions.View> {
        val viewModel = hiltViewModel<SubscriptionsScreenViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SubscriptionsScreen(
            state = state,
            onEvent = { event ->
                when(event) {
                    is SubscriptionsScreenUiEvent.ChannelClicked -> subscriptionsBackStack.add(Route.Channel(event.channelId))
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state.error == NetworkError.AUTHENTICATION) {
                rootBackStack.add(Route.Authentication)
            }
        }
    }
}