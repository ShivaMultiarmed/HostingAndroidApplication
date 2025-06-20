package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.channel.create.CreateChannelScreen
import mikhail.shell.video.hosting.presentation.channel.create.CreateChannelViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.createChannelRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Channel.Create> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<CreateChannelViewModel, CreateChannelViewModel.Factory> {
            it.create(userId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        CreateChannelScreen(
            state = state,
            onSubmit = {
                viewModel.createChannel(it)
            },
            onSuccess = {
                coroutineScope.launch {
                    navController.navigate(Route.Channel.View(it.channelId!!))
                }
            },
            onPopup = {
                navController.popBackStack()
            }
        )
    }
}