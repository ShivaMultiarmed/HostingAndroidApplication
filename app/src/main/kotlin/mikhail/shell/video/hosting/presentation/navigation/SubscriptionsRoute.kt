package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreen
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenViewModel

fun NavGraphBuilder.subscriptionsRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Subscriptions> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<SubscriptionsScreenViewModel, SubscriptionsScreenViewModel.Factory> {
            it.create(userId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        SubscriptionsScreen(
            state = state,
            onRefresh = {
                viewModel.loadChannels()
            },
             onChannelClick = {
                 navController.navigate(Route.Channel(it))
             }
        )
    }
}