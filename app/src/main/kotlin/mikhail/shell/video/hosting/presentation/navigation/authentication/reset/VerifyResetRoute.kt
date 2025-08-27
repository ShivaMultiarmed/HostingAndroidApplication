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
import mikhail.shell.video.hosting.presentation.reset.VerifyResetScreen
import mikhail.shell.video.hosting.presentation.reset.VerifyResetScreenState
import mikhail.shell.video.hosting.presentation.reset.VerifyResetViewModel

fun NavGraphBuilder.verifyResetRoute(
    navController: NavController
) {
    composable<Route.Authentication.Reset.Verification> {
        val bundle = it.toRoute<Route.Authentication.Reset.Verification>()
        val viewModel = hiltViewModel<VerifyResetViewModel, VerifyResetViewModel.Factory> { it.create(bundle.userName) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VerifyResetScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is VerifyResetScreenState.Success) {
                navController.navigate(Route.Authentication.Reset.Confirmation((state as VerifyResetScreenState.Success).token))
            } else if (state is VerifyResetScreenState.Expired) {
                navController.popBackStack()
            }
        }
    }
}