package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.reset.VerifyResetScreen
import mikhail.shell.video.hosting.presentation.reset.VerifyResetScreenState
import mikhail.shell.video.hosting.presentation.reset.VerifyResetViewModel

fun EntryProviderBuilder<Route>.verifyResetRoute(
    resettingBackStack: MutableList<Route>
) {
    entry<Route.Authentication.Reset.Verification> { bundle ->
        val viewModel = hiltViewModel<VerifyResetViewModel, VerifyResetViewModel.Factory> { it.create(bundle.userName) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VerifyResetScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is VerifyResetScreenState.Success) {
                resettingBackStack.add(Route.Authentication.Reset.Confirmation((state as VerifyResetScreenState.Success).token))
                resettingBackStack.removeAt(resettingBackStack.size - 2)
            } else if (state is VerifyResetScreenState.Expired) {
                resettingBackStack.removeLastOrNull()
            }
        }
    }
}