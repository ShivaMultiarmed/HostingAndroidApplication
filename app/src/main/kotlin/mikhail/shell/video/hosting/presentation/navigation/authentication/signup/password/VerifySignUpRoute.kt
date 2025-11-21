package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationEvent
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationViewModel
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationScreen
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.verifySignUpRoute(
    signUpBackStack: MutableList<Route>
) {
    entry<Route.Authentication.SignUp.Verification> { bundle ->
        val viewModel = hiltViewModel<SignUpVerificationViewModel, SignUpVerificationViewModel.Factory> { factory ->
            factory.create(bundle.userName)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val context = LocalContext.current
        val snackBarHostState = remember { SnackbarHostState() }
        SignUpVerificationScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is SignUpVerificationEvent.Failure -> {
                    val errMsg = if (event.error is NetworkError) {
                        context.getNetworkErrorMessage(event.error)
                    } else context.getString(R.string.unexpected_error)
                    snackBarHostState.showSnackbar(message = errMsg, duration = SnackbarDuration.Short)
                }
                is SignUpVerificationEvent.Success -> {
                    signUpBackStack.clear()
                    signUpBackStack.add(
                        Route.Authentication.SignUp.Confirmation(event.token)
                    )
                }
            }
        }
    }
}