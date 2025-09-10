package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInUiEvent
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel

fun EntryProviderBuilder<Route>.signInRoute(
    rootBackStack: MutableList<Route>,
    authBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry(Route.Authentication.SignIn) {
        val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
        val state by viewModel.state.collectAsState()
        SignInScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    SignInUiEvent.SignUp -> authBackStack.add(Route.Authentication.SignUp)
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state.authModel != null) {
                userDetailsProvider.save(
                    state.authModel!!.let {
                        UserDetails(
                            userId = it.userId,
                            token = it.token
                        )
                    }
                )
                rootBackStack.add(Route.Recommendations)
                rootBackStack.remove(Route.Authentication)
            }
        }
    }
}
