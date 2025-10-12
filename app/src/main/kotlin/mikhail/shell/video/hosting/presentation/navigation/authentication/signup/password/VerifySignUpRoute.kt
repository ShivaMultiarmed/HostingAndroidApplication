package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpScreenState
import mikhail.shell.video.hosting.presentation.signup.password.VerifySignUpViewModel

fun EntryProviderScope<Route>.verifySignUpRoute(
    signUpBackStack: MutableList<Route>
) {
    entry<Route.Authentication.SignUp.Verification> { bundle ->
        val viewModel = hiltViewModel<VerifySignUpViewModel, VerifySignUpViewModel.Factory> { it.create(bundle.userName) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VerifySignUpScreen(
            state = state,
            onEvent = { event ->
                viewModel.onEvent(event)
            }
        )
        LaunchedEffect(state) {
            if (state is VerifySignUpScreenState.Success) {
                signUpBackStack.add(
                    Route.Authentication.SignUp.Confirmation(
                        (state as VerifySignUpScreenState.Success).token
                    )
                )
                signUpBackStack.removeAt(signUpBackStack.size - 2)
            } else if (state is VerifySignUpScreenState.Expired) {
                signUpBackStack.removeLastOrNull()
            }
        }
    }
}