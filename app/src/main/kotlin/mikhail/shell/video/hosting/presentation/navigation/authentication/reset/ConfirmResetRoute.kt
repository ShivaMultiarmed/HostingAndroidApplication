package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetScreen
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetScreenState
import mikhail.shell.video.hosting.presentation.reset.ConfirmResetViewModel

fun EntryProviderBuilder<Route>.confirmResetRoute(
    rootBackStack: MutableList<Route>,
    resettingBackStack: MutableList<Route>
) {
    entry<Route.Authentication.Reset.Confirmation> { bundle ->
        val viewModel = hiltViewModel<ConfirmResetViewModel, ConfirmResetViewModel.Factory> { it.create(bundle.token) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        ConfirmResetScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is ConfirmResetScreenState.Success) {
                rootBackStack.add(Route.Recommendations)
                rootBackStack.remove(Route.Authentication)
            } else if (state is ConfirmResetScreenState.Expiration) {
                resettingBackStack.removeLastOrNull()
            }
        }
    }
}