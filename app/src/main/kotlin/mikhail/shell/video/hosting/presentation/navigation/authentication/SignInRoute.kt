package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenState
import mikhail.shell.video.hosting.presentation.signin.password.SignInUiEvent
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.signInRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Authentication.SignIn> {
        val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SignInScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    SignInUiEvent.SignUp -> navController.navigate(Route.Authentication.SignUp)
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is SignInScreenState.Success) {
                userDetailsProvider.save(
                    (state as SignInScreenState.Success).authModel.let {
                        UserDetails(
                            userId = it.userId,
                            token = it.token
                        )
                    }
                )
                delay(1.seconds)
                navController.navigate(Route.Video.Recommendations)
            }
        }
    }
}
