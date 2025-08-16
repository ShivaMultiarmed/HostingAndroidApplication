package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreen
import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenViewModel
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.subscriptionsRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.User.Subscriptions> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<SubscriptionsScreenViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        SubscriptionsScreen(
            state = state,
            onRefresh = viewModel::loadChannels,
            onChannelClick = {
                navController.navigate(Route.Channel.View(it))
            },
            onUserNotFound = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.navigate(Route.Authentication)
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