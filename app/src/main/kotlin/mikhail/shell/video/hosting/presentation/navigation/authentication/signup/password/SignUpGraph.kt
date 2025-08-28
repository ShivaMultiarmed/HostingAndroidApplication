package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderBuilder<Route>.signUpGraph(
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
            entryDecorators = listOf(), // TODO
            entryProvider = entryProvider {
                requestSignUpRoute(signUpBackStack)
                verifySignUpRoute(signUpBackStack)
                confirmSignUpRoute(rootBackStack)
            }
        )
    }
}