package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.RequestResetScreen
import mikhail.shell.video.hosting.presentation.reset.ResetRequestingScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.reset.ResetRequestingViewModel
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.resetRequestingRoute(
    authBackStack: MutableList<Route>,
    resettingBackStack: SnapshotStateList<Route>
) {
    entry<Route.Authentication.Reset.Request> {
        val context = LocalContext.current
        val viewModel = hiltViewModel<ResetRequestingViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        RequestResetScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ScreenEvent.Success -> resettingBackStack.add(Route.Authentication.Reset.Verification(event.userId))
                is ScreenEvent.Failure -> coroutineScope.launch {
                    val errMsg = when (event.error) {
                        is NetworkError -> context.getNetworkErrorMessage(event.error)
                        else -> context.getString(R.string.unexpected_error)
                    }
                    snackBarHostState.showSnackbar(errMsg)
                }
                ScreenEvent.NavigateBack -> authBackStack.removeLastOrNull()
            }
        }
    }
}