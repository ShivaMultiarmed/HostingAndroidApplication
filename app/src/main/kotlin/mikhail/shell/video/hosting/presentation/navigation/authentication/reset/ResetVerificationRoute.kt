package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationScreen
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationViewModel
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.resetVerificationRoute(
    resettingBackStack: MutableList<Route>
) {
    entry<Route.Authentication.Reset.Verification> { route ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val viewModel = hiltViewModel<ResetVerificationViewModel, ResetVerificationViewModel.Factory> { factory ->
            factory.create(route.userId)
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
                is ScreenEvent.Failure -> coroutineScope.launch {
                    val errMsg = when (event.error) {
                        is NetworkError -> context.getNetworkErrorMessage(event.error)
                        else -> context.getString(R.string.unexpected_error)
                    }
                    snackBarHostState.showSnackbar(message = errMsg)
                }
                is ScreenEvent.Success -> {
                    resettingBackStack.clear()
                    resettingBackStack.add(Route.Authentication.Reset.Confirmation(event.token))
                }
            }
        }
    }
}