package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.authentication.reset.resetGraph
import mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password.signUpGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderBuilder<Route>.authenticationGraph(
    rootBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Authentication> {
        val authBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Authentication.SignIn)
        }
        NavDisplay(
            backStack = authBackStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                signInRoute(
                    rootBackStack = rootBackStack,
                    authBackStack = authBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                signUpGraph(
                    rootBackStack = rootBackStack,
                    authBackStack = authBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                resetGraph(
                    rootBackStack = rootBackStack
                )
            }
        )
    }
}