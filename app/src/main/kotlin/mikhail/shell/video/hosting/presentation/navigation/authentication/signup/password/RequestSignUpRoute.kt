package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.RequestSignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.RequestSignUpViewModel

fun EntryProviderBuilder<Route>.requestSignUpRoute(
    signUpBackStack: SnapshotStateList<Route>
) {
    entry<Route.Authentication.SignUp.Request> {
        val viewModel = hiltViewModel<RequestSignUpViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        RequestSignUpScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state.isAccepted) {
            if (state.isAccepted) {
                signUpBackStack.add(Route.Authentication.SignUp.Verification(state.userName))
            }
        }
    }
}