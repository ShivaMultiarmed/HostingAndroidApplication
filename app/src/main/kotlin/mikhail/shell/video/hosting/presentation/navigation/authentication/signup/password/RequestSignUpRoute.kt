package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.RequestSignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.RequestSignUpViewModel

fun NavGraphBuilder.requestSignUpRoute(
    navController: NavController
) {
    composable<Route.Authentication.SignUp.Request> {
        val viewModel = hiltViewModel<RequestSignUpViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        RequestSignUpScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state.isAccepted) {
            if (state.isAccepted) {
                navController.navigate(Route.Authentication.SignUp.Verification(state.userName))
            }
        }
    }
}