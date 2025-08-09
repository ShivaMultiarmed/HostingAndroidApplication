package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.channel.edit.EditChannelScreen
import mikhail.shell.video.hosting.presentation.channel.edit.EditChannelViewModel
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.editChannelRoute(
    navController: NavController
) {
    composable<Route.Channel.Edit> {
        val data = it.toRoute<Route.Channel.Edit>()
        val viewModel = hiltViewModel<EditChannelViewModel, EditChannelViewModel.Factory> { it.create(data.channelId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        EditChannelScreen(
            state = state,
            onPopup = navController::popBackStack,
            onSubmit = viewModel::editChannel,
            onSuccess = {
                navController.navigate(Route.Channel.View(it.channelId!!))
            },
            onChannelNotFound = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.popBackStack()
                }
            },
            onAuthenticationRequired = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.navigate(Route.Authentication)
                }
            }
        )
    }
}