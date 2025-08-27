package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetScreen
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetScreenState
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetViewModel

fun NavGraphBuilder.confirmResetRoute(
    navController: NavController
) {
    composable<Route.Authentication.Reset.Confirmation> {
        val bundle = it.toRoute<Route.Authentication.Reset.Confirmation>()
        val viewModel = hiltViewModel<ConfirmResetViewModel, ConfirmResetViewModel.Factory> { it.create(bundle.token) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        ConfirmResetScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is ConfirmResetScreenState.Success) {
                navController.navigate(Route.Video.Recommendations)
            } else if (state is ConfirmResetScreenState.Expiration) {
                navController.popBackStack()
                navController.popBackStack()
            }
        }
    }
}