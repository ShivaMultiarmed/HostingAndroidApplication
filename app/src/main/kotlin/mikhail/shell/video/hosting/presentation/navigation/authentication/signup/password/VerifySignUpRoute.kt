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
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpViewModel
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.verifySignUpRoute(
    navController: NavController
) {
    composable<Route.Authentication.SignUp.Verification> {
        val bundle = it.toRoute<Route.Authentication.SignUp.Verification>()
        val userName = bundle.userName
        val viewModel = hiltViewModel<VerifySignUpViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        VerifySignUpScreen(
            userName = userName,
            state = state,
            onVerify = { code ->
                viewModel.verify(userName, code)
            },
            onSuccess = { token ->
                coroutineScope.launch {
                    delay(0.5.seconds)
                    navController.navigate(Route.Authentication.SignUp.Confirmation(token, userName))
                }
            }
        )
    }
}