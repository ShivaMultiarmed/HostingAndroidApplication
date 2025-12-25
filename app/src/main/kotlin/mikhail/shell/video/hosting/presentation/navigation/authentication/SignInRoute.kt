package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInViewModel
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.signInRoute(
    rootBackStack: MutableList<Route>,
    authBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry(Route.Authentication.SignIn) {
        val context = LocalContext.current
        val viewModel = hiltViewModel<SignInViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        SignInScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                ScreenEvent.SignUpRequested -> authBackStack.add(Route.Authentication.SignUp)
                ScreenEvent.ResetRequested -> authBackStack.add(Route.Authentication.Reset)
                is ScreenEvent.Failure -> {
                    context.getStandardErrorMessage(event.error)?.let {
                        snackBarHostState.showSnackbar(it)
                    }
                }
                is ScreenEvent.Success -> {
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
}
