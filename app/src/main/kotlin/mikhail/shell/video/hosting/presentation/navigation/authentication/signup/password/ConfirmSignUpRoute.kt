package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpViewModel
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.confirmSignUpRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Authentication.SignUp.Confirmation> {
        val bundle = it.toRoute<Route.Authentication.SignUp.Confirmation>()
        val viewModel = hiltViewModel<ConfirmSignUpViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ConfirmSignUpScreen(
            token = bundle.token,
            state = state,
            onConfirm = viewModel::confirm,
            onSuccess = {
                coroutineScope.launch {
                    userDetailsProvider.save(
                        UserDetails(
                            userId = it.userId,
                            token = it.token
                        )
                    )
                    delay(1.seconds)
                    navController.navigate(Route.Video.Recommendations)
                }
            }
        )
    }
}