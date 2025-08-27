package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.RequestResetScreen
import mikhail.shell.video.hosting.presentation.reset.RequestResetScreenState
import mikhail.shell.video.hosting.presentation.reset.RequestResetUiEvent
import mikhail.shell.video.hosting.presentation.reset.RequestResetViewModel

fun NavGraphBuilder.requestResetRoute(
    navController: NavController
) {
    composable<Route.Authentication.Reset.Request> {
        val viewModel = hiltViewModel<RequestResetViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        RequestResetScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    RequestResetUiEvent.Cancel -> navController.popBackStack()
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is RequestResetScreenState.Success) {
                navController.navigate(Route.Authentication.Reset.Verification((state as RequestResetScreenState.Success).userName))
            }
        }
    }
}