package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators

fun EntryProviderScope<Route>.signingUpGraph(
    rootBackStack: MutableList<Route>,
    authBackStack: SnapshotStateList<Route>
) {
    entry<Route.Authentication.SignUp> {
        val signUpBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Authentication.SignUp.Request)
        }
        NavDisplay(
            backStack = signUpBackStack,
            entryDecorators = defaultNavDecorators,
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.predictiveBackAnimation },
            entryProvider = entryProvider {
                signingUpRequestingRoute(
                    authBackStack = authBackStack,
                    signUpBackStack = signUpBackStack
                )
                signingUpVerificationRoute(signUpBackStack = signUpBackStack)
                signingUpConfirmationRoute(
                    rootBackStack = rootBackStack,
                    signUpBackStack = signUpBackStack
                )
            }
        )
    }
}