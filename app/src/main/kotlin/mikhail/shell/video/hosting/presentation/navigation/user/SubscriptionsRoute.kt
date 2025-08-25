package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreen
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenUiEvent
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenViewModel

fun NavGraphBuilder.subscriptionsRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.User.Subscriptions> {
        val viewModel = hiltViewModel<SubscriptionsScreenViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SubscriptionsScreen(
            state = state,
            onEvent = {
                when(it) {
                    is SubscriptionsScreenUiEvent.ClickedChannel -> navController.navigate(Route.Channel.View(it.channelId))
                    else -> viewModel.onEvent(it)
                }
            }
        )
        LaunchedEffect(state) {
            if (state.error == NetworkError.AUTHENTICATION) {
                navController.navigate(Route.Authentication)
            }
        }
    }
}