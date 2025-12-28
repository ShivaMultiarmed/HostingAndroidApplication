package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationScreen
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationViewModel
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.resetVerificationRoute(
    resettingBackStack: MutableList<Route>
) {
    entry<Route.Authentication.Reset.Verification> { route ->
        val context = LocalContext.current
        val viewModel = hiltViewModel<ResetVerificationViewModel, ResetVerificationViewModel.Factory> { factory ->
            factory.create(route.userId, route.userName)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        ResetVerificationScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ScreenEvent.Failure -> {
                    context.getStandardErrorMessage(event.error)?.let {
                        snackBarHostState.showSnackbar(it)
                    }
                }
                is ScreenEvent.Success -> {
                    resettingBackStack.removeLastOrNull()
                    resettingBackStack.add(Route.Authentication.Reset.Confirmation(event.token))
                }
            }
        }
    }
}