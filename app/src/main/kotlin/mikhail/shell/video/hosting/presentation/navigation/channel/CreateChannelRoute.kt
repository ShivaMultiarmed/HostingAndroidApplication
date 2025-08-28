package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreen
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationUiEvent
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import kotlin.time.Duration.Companion.seconds

fun EntryProviderBuilder<Route>.createChannelRoute(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>
) {
    entry <Route.User.CreateChannel> {
        val viewModel = hiltViewModel<ChannelCreationViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ChannelCreationScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    is ChannelCreationUiEvent.Success -> {
                        userBackStack.add(Route.Channel.View(event.channelId))
                        userBackStack.removeAt(userBackStack.size - 2)
                    }
                    is ChannelCreationUiEvent.AuthenticationRequired -> coroutineScope.launch {
                        delay(1.seconds)
                        userBackStack.add(Route.Authentication)
                    }
                    is ChannelCreationUiEvent.Cancel -> rootBackStack.removeLastOrNull()
                    else -> viewModel.onEvent(event)
                }
            }
        )
    }
}