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
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel

fun NavGraphBuilder.signInRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Authentication.SignIn> {
        val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SignInScreen(
            state = state,
            onSubmit = viewModel::signIn,
            onSuccess = {
                userDetailsProvider.save(
                    UserDetails(
                        userId = it.userId,
                        token = it.token
                    )
                )
                navController.navigate(Route.Video.Recommendations)
                viewModel.subscribeToNotifications()
            },
            onSigningUp = {
                navController.navigate(Route.Authentication.SignUp)
            }
        )
    }
}
