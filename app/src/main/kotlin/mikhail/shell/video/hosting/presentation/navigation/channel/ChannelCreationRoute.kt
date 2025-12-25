package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreen
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.channelCreationRoute(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>
) {
    entry <Route.User.ChannelCreation> { route ->
        val context = LocalContext.current
        val viewModel = hiltViewModel<ChannelCreationViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        ChannelCreationScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                ScreenEvent.Cancelled -> userBackStack.removeLastOrNull()
                is ScreenEvent.Created -> {
                    userBackStack.removeLastOrNull()
                    userBackStack.add(Route.Channel(event.channelId))
                }
                is ScreenEvent.Failure -> {
                    if (event.error == NetworkError.AUTHENTICATION) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        context.getStandardErrorMessage(event.error)?.let {
                            snackBarHostState.showSnackbar(it)
                        }
                    }
                }
            }
        }
    }
}