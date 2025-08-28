package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import kotlinx.coroutines.delay
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenState
import mikhail.shell.video.hosting.presentation.signin.password.SignInUiEvent
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel
import kotlin.time.Duration.Companion.seconds

fun EntryProviderBuilder<Route>.signInRoute(
    rootBackStack: SnapshotStateList<Route>,
    authBackStack: SnapshotStateList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry(Route.Authentication.SignIn) {
        val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
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
            if (state is SignInScreenState.Success) {
                userDetailsProvider.save(
                    (state as SignInScreenState.Success).authModel.let {
                        UserDetails(
                            userId = it.userId,
                            token = it.token
                        )
                    }
                )
                delay(1.seconds)
                authBackStack.clear()
                rootBackStack.remove(Route.Authentication.SignIn)
                rootBackStack.add(Route.Recommendations)
            }
        }
    }
}
