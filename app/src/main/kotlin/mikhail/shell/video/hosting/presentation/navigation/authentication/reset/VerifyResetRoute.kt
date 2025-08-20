package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

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
import mikhail.shell.video.hosting.presentation.reset.VerifyResetScreen
import mikhail.shell.video.hosting.presentation.reset.VerifyResetViewModel
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.verifyResetRoute(
    navController: NavController
) {
    composable<Route.Authentication.Reset.Verification> {
        val bundle = it.toRoute<Route.Authentication.Reset.Verification>()
        val viewModel = hiltViewModel<VerifyResetViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        VerifyResetScreen(
            state = state,
            onVerify = { code ->
                viewModel.verify(bundle.userName, code)
            },
            onSuccess = { token ->
                coroutineScope.launch {
                    delay(0.8.seconds)
                    navController.navigate(Route.Authentication.Reset.Confirmation(token))
                }
            },
            onExpiration = {
                coroutineScope.launch {
                    delay(1.seconds)
                    navController.popBackStack()
                }
            }
        )
    }
}