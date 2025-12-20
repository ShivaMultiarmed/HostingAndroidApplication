package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

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
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationScreen
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationViewModel
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.signingUpConfirmationRoute(
    rootBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Authentication.SignUp.Confirmation> { route ->
        val viewModel = hiltViewModel<SignUpConfirmationViewModel, SignUpConfirmationViewModel.Factory> { factory ->
            factory.create(route.token)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        SignUpConfirmationScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            if (event is ScreenEvent.Failure) {
                val errMsg = if (event.error is NetworkError) {
                    context.getNetworkErrorMessage(event.error)
                } else {
                     context.getString(R.string.unexpected_error)
                }
                snackBarHostState.showSnackbar(errMsg)
            } else if (event is ScreenEvent.Success) {
                userDetailsProvider.save(
                    UserDetails(
                        userId = event.authModel.userId,
                        token = event.authModel.token
                    )
                )
                rootBackStack.remove(Route.Authentication)
                if (rootBackStack.isEmpty()) {
                    rootBackStack.add(Route.Recommendations)
                }
            }
        }
    }
}