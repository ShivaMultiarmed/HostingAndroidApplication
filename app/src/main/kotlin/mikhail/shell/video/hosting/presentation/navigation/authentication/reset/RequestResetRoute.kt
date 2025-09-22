package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.RequestResetScreen
import mikhail.shell.video.hosting.presentation.reset.RequestResetScreenState
import mikhail.shell.video.hosting.presentation.reset.RequestResetUiEvent
import mikhail.shell.video.hosting.presentation.reset.RequestResetViewModel

fun EntryProviderBuilder<Route>.requestResetRoute(
    resettingBackStack: MutableList<Route>
) {
    entry<Route.Authentication.Reset.Request> {
        val viewModel = hiltViewModel<RequestResetViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        RequestResetScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    RequestResetUiEvent.Cancel -> resettingBackStack.removeLastOrNull()
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is RequestResetScreenState.Success) {
                resettingBackStack.add(Route.Authentication.Reset.Verification((state as RequestResetScreenState.Success).userName))
            }
        }
    }
}