package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.authentication.reset.resetGraph
import mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password.signingUpGraph
import mikhail.shell.video.hosting.presentation.navigation.common.RootAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators

fun EntryProviderScope<Route>.authenticationGraph(
    rootBackStack: MutableList<Route>
) {
    entry<Route.Authentication> {
        val authBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Authentication.SignIn)
        }
        NavDisplay(
            backStack = authBackStack,
            entryDecorators = defaultNavDecorators,
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.leavingAnimation },
            entryProvider = entryProvider {
                signInRoute(
                    rootBackStack = rootBackStack,
                    authBackStack = authBackStack
                )
                signingUpGraph(
                    authBackStack = authBackStack,
                    rootBackStack = rootBackStack
                )
                resetGraph(
                    authBackStack = authBackStack,
                    rootBackStack = rootBackStack
                )
            }
        )
    }
}