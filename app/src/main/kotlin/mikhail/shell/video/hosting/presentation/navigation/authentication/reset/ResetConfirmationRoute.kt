package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetScreen
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationViewModel
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.resetConfirmationRoute(
    rootBackStack: MutableList<Route>,
    resettingBackStack: MutableList<Route>
) {
    entry<Route.Authentication.Reset.Confirmation> { route ->
        val context = LocalContext.current
        val viewModel =
            hiltViewModel<ResetConfirmationViewModel, ResetConfirmationViewModel.Factory> { factory ->
                factory.create(route.token)
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        ConfirmResetScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ScreenEvent.Failure -> {
                    if (event.error != NetworkError.AUTHENTICATION) {
                        context.getStandardErrorMessage(event.error)?.let {
                            snackBarHostState.showSnackbar(it)
                        }
                    } else {
                        snackBarHostState.showSnackbar(
                            context.getString(R.string.code_not_valid)
                        )
                        resettingBackStack.removeLastOrNull()
                    }
                }
                is ScreenEvent.Success -> {
                    rootBackStack.remove(Route.Authentication)
                    if (rootBackStack.isEmpty()) {
                        rootBackStack.add(Route.Recommendations)
                    }
                }
            }
        }
    }
}