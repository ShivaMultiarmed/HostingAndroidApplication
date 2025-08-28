package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpScreenState
import mikhail.shell.video.hosting.presentation.signup.password.ConfirmSignUpViewModel

fun EntryProviderBuilder<Route>.confirmSignUpRoute(
    rootBackStack: MutableList<Route>
) {
    entry<Route.Authentication.SignUp.Confirmation> { bundle ->
        val viewModel = hiltViewModel<ConfirmSignUpViewModel, ConfirmSignUpViewModel.Factory> { it.create(bundle.token) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        ConfirmSignUpScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is ConfirmSignUpScreenState.Success) {
                rootBackStack.add(Route.Recommendations)
                rootBackStack.remove(Route.Authentication)
            } else if (state is ConfirmSignUpScreenState.Expired) {
                rootBackStack.removeLastOrNull()
            }
        }
    }
}