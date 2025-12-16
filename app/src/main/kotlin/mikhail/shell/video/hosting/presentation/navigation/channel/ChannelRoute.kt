package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenEvent
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.channelRoute(
    rootBackStack: MutableList<Route>,
    channelBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Channel.View> { route ->
        val context = LocalContext.current
        val userId by rememberSaveable { mutableLongStateOf(userDetailsProvider.getUserId()) }
        val channelId = route.channelId
        val viewModel = hiltViewModel<ChannelScreenViewModel, ChannelScreenViewModel.Factory> {
            it.create(channelId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        ChannelScreen(
            userId = userId,
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ChannelScreenEvent.EditingRequest -> channelBackStack.add(Route.Channel.Edit(channelId))
                is ChannelScreenEvent.VideoChosen -> rootBackStack.add(Route.Video(event.videoId))
                is ChannelScreenEvent.Failure -> {
                    if (event.error == NetworkError.AUTHENTICATION) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        context.getStandardErrorMessage(event.error)?.let {
                            snackBarHostState.showSnackbar(it)
                        }
                    }
                }
                is ChannelScreenEvent.Removed -> channelBackStack.removeLastOrNull() // TODO: replace with current tab backstack
            }
        }
    }
}