package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderScope<Route>.signUpGraph(
    rootBackStack: MutableList<Route>,
    authBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Authentication.SignUp> {
        val signUpBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Authentication.SignUp.Request)
        }
        NavDisplay(
            backStack = signUpBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                requestSignUpRoute(signUpBackStack)
                verifySignUpRoute(signUpBackStack)
                confirmSignUpRoute(rootBackStack)
            }
        )
    }
}