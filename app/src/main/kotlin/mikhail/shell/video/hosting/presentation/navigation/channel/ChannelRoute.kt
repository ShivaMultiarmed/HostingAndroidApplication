package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenState
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenUiEvent
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun EntryProviderBuilder<Route>.channelRoute(
    rootBackStack: MutableList<Route>,
    channelBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry <Route.Channel.View> { route ->
        val userId = userDetailsProvider.getUserId()
        val channelId = route.channelId
        val viewModel = hiltViewModel<ChannelScreenViewModel, ChannelScreenViewModel.Factory> { it.create(channelId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ChannelScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    is ChannelScreenUiEvent.ClickVideo -> rootBackStack.add(Route.Video(event.videoId))
                    ChannelScreenUiEvent.Edit -> channelBackStack.add(Route.Channel.Edit(channelId))
                    ChannelScreenUiEvent.Remove -> coroutineScope.launch {
                        viewModel.onEvent(event)
                        delay(800.milliseconds)
                        channelBackStack.removeLastOrNull()
                    }
                    else -> viewModel.onEvent(event)
                }
            },
            userId = userId
        )
        LaunchedEffect(state) {
            if (state is ChannelScreenState.Failure) {
                if ((state as ChannelScreenState.Failure).error == NetworkError.NOT_FOUND) {
                    coroutineScope.launch {
                        delay(1.seconds)
                        channelBackStack.removeLastOrNull()
                    }
                } else if ((state as ChannelScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    rootBackStack.add(Route.Authentication)
                }
            }
        }
    }
}