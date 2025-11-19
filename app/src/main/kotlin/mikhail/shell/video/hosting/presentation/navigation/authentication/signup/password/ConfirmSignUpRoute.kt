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
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationEvent
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationViewModel
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.confirmSignUpRoute(
    rootBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Authentication.SignUp.Confirmation> { bundle ->
        val viewModel = hiltViewModel<SignUpConfirmationViewModel, SignUpConfirmationViewModel.Factory> { it.create(bundle.token) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        ConfirmSignUpScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            if (event is SignUpConfirmationEvent.Failure) {
                val errMsg = if (event.error is NetworkError) {
                    context.getNetworkErrorMessage(event.error)
                } else {
                     context.getString(R.string.unexpected_error)
                }
                snackBarHostState.showSnackbar(message = errMsg, duration = SnackbarDuration.Short)
            } else if (event is SignUpConfirmationEvent.Success) {
                userDetailsProvider.save(
                    UserDetails(
                        userId = event.authModel.userId,
                        token = event.authModel.token
                    )
                )
                rootBackStack.remove(Route.Authentication)
                rootBackStack.add(Route.Recommendations)
            }
        }
    }
}