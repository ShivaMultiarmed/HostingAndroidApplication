package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.RequestResetScreen
import mikhail.shell.video.hosting.presentation.reset.RequestResetViewModel
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.requestResetRoute(
    navController: NavController
) {
    composable<Route.Authentication.Reset.Request> {
        val viewModel = hiltViewModel<RequestResetViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        RequestResetScreen(
            state = state,
            onRequest = viewModel::request,
            onSuccess = { userName ->
                coroutineScope.launch {
                    delay(0.8.seconds)
                    navController.navigate(Route.Authentication.Reset.Verification(userName))
                }
            }
        )
    }
}