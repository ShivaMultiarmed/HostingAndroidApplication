package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.RequestSignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.RequestSignUpViewModel
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.requestSignUpRoute(
    navController: NavController
) {
    composable<Route.Authentication.SignUp.Request> {
        val viewModel = hiltViewModel<RequestSignUpViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        RequestSignUpScreen(
            state = state,
            onSuccess = {
                coroutineScope.launch {
                    delay(0.5.seconds)
                    navController.navigate(Route.Authentication.SignUp.Verification(it))
                }
            }
        )
    }
}