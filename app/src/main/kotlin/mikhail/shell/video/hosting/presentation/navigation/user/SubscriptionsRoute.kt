package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreen
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenEvent
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenViewModel
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.subscriptionsRoute(
    rootBackStack: MutableList<Route>,
    subscriptionsBackStack: MutableList<Route>
) {
    entry <Route.Subscriptions.View> {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val viewModel = hiltViewModel<SubscriptionsScreenViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        SubscriptionsScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is SubscriptionsScreenEvent.ChannelChosen -> subscriptionsBackStack.add(Route.Channel(event.channelId))
                is SubscriptionsScreenEvent.Failure -> {
                    if (event.error == NetworkError.AUTHENTICATION) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        context.getStandardErrorMessage(event.error)?.let {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(it)
                            }
                        }
                    }
                }
            }
        }
    }
}