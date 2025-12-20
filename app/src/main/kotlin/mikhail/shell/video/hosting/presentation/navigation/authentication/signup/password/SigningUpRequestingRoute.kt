package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.SignUpRequestingScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.signup.password.SignUpRequestingScreen
import mikhail.shell.video.hosting.presentation.signup.password.SignUpRequestingViewModel
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.signingUpRequestingRoute(
    authBackStack: SnapshotStateList<Route>,
    signUpBackStack: SnapshotStateList<Route>
) {
    entry<Route.Authentication.SignUp.Request> {
        val viewModel = hiltViewModel<SignUpRequestingViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        SignUpRequestingScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                ScreenEvent.Cancel -> authBackStack.removeLastOrNull()
                is ScreenEvent.Failure -> {
                    val errorMessage = when (event.error) {
                        is NetworkError -> context.getNetworkErrorMessage(event.error)
                        else -> context.getString(R.string.unexpected_error)
                    }
                    snackBarHostState.showSnackbar(message = errorMessage, duration = SnackbarDuration.Short)
                }
                is ScreenEvent.Success -> signUpBackStack.add(
                    Route.Authentication.SignUp.Verification(event.userName)
                )
            }
        }
    }
}