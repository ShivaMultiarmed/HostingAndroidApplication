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
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetScreen
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationViewModel
import mikhail.shell.video.hosting.presentation.utils.observe

fun EntryProviderScope<Route>.resetConfirmationRoute(
    rootBackStack: MutableList<Route>,
    resettingBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Authentication.Reset.Confirmation> { route ->
        val context = LocalContext.current
        val viewModel = hiltViewModel<ResetConfirmationViewModel, ResetConfirmationViewModel.Factory> { factory ->
            factory.create(route.token)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        ConfirmResetScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ScreenEvent.Failure -> {
                    val errMsg = when(event.error) {
                        is NetworkError -> context.getNetworkErrorMessage(event.error)
                        else -> context.getString(R.string.unexpected_error)
                    }
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(errMsg)
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
                    rootBackStack.add(Route.Recommendations)
                }
            }
        }
    }
}