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
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpScreenState
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpViewModel

fun NavGraphBuilder.verifySignUpRoute(
    navController: NavController
) {
    composable<Route.Authentication.SignUp.Verification> {
        val bundle = it.toRoute<Route.Authentication.SignUp.Verification>()
        val userName = bundle.userName
        val viewModel = hiltViewModel<VerifySignUpViewModel, VerifySignUpViewModel.Factory> { it.create(userName) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VerifySignUpScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is VerifySignUpScreenState.Success) {
                navController.navigate(
                    Route.Authentication.SignUp.Confirmation(
                        (state as VerifySignUpScreenState.Success).token
                    )
                )
            }
        }
    }
}