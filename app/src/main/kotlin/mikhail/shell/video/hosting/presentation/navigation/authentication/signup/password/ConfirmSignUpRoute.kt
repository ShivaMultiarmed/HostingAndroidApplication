package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpScreenState
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpViewModel

fun NavGraphBuilder.confirmSignUpRoute(
    navController: NavController
) {
    composable<Route.Authentication.SignUp.Confirmation> {
        val bundle = it.toRoute<Route.Authentication.SignUp.Confirmation>()
        val viewModel = hiltViewModel<ConfirmSignUpViewModel, ConfirmSignUpViewModel.Factory> { it.create(bundle.token) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        ConfirmSignUpScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is ConfirmSignUpScreenState.Success) {
                navController.navigate(Route.Video.Recommendations)
            } else if (state is ConfirmSignUpScreenState.Expired) {
                navController.popBackStack()
                navController.popBackStack()
            }
        }
    }
}