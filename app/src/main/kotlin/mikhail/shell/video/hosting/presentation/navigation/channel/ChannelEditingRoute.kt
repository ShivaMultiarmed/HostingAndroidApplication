package mikhail.shell.video.hosting.presentation.navigation.channel

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
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreen
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.channelEditingRoute(
    rootBackStack: MutableList<Route>,
    channelBackStack: MutableList<Route>
) {
    entry <Route.Channel.Edit> { route ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val viewModel = hiltViewModel<ChannelEditingViewModel, ChannelEditingViewModel.Factory> { factory ->
            factory.create(route.channelId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        ChannelEditingScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                ScreenEvent.Cancelled -> channelBackStack.removeLastOrNull()
                is ScreenEvent.Failure -> {
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
                ScreenEvent.Success -> {
                    channelBackStack.removeIf { it is Route.Channel.View }
                    channelBackStack.remove(route)
                    channelBackStack.add(Route.Channel.View(route.channelId))
                }
            }
        }
    }
}