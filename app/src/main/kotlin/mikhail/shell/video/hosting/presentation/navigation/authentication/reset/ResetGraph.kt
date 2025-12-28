package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations

fun EntryProviderScope<Route>.resetGraph(
    rootBackStack: MutableList<Route>,
    authBackStack: SnapshotStateList<Route>
) {
    entry<Route.Authentication.Reset> {
        val resettingBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Authentication.Reset.Request)
        }
        NavDisplay(
            backStack = resettingBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.leavingAnimation },
            entryProvider = entryProvider {
                resetRequestingRoute(
                    authBackStack = authBackStack,
                    resettingBackStack = resettingBackStack
                )
                resetVerificationRoute(resettingBackStack = resettingBackStack)
                resetConfirmationRoute(
                    rootBackStack = rootBackStack,
                    resettingBackStack = resettingBackStack
                )
            }
        )
    }
}