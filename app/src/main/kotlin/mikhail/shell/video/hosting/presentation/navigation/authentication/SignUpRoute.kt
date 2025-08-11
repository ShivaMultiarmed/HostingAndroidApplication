package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.SignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.SignUpWithPasswordViewModel

fun NavGraphBuilder.signUpRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Authentication.SignUp> {
        val viewModel = hiltViewModel<SignUpWithPasswordViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        SignUpScreen(
            state = state,
            onSubmit = viewModel::signUp,
            onSuccess = {
                userDetailsProvider.save(
                    UserDetails(
                        userId = it.userId,
                        token = it.token
                    )
                )
                navController.navigate(Route.Video.Search)
            }
        )
    }
}