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
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetScreen
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetViewModel
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.confirmResetRoute(
    navController: NavController
) {
    composable<Route.Authentication.Reset.Confirmation> {
        val bundle = it.toRoute<Route.Authentication.Reset.Confirmation>()
        val viewModel = hiltViewModel<ConfirmResetViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ConfirmResetScreen(
            state = state,
            onConfirm = { password, passwordDuplicate ->
                viewModel.confirm(
                    token = bundle.token,
                    password = password,
                    passwordDuplicate = passwordDuplicate
                )
            },
            onSuccess = {
                coroutineScope.launch {
                    delay(0.8.seconds)
                    navController.navigate(Route.Authentication.SignIn)
                }
            },
            onExpiration = {
                coroutineScope.launch {
                    delay(1.seconds)
                    navController.popBackStack()
                    navController.popBackStack()
                }
            }
        )
    }
}